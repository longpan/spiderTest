package com.ongl.chen.utils.spider.service;

import com.ongl.chen.utils.spider.beans.cbg.CbgSpiderTask;
import com.ongl.chen.utils.spider.downloader.cbg.CbgMhxySeleniuDownloader;
import com.ongl.chen.utils.spider.exception.AuthInvalidException;
import com.ongl.chen.utils.spider.processor.cbg.CbgPetDetailPageProcessor;
import com.ongl.chen.utils.spider.processor.cbg.CbgPetListPageProcessor;
import com.ongl.chen.utils.spider.utils.AppConfigFromPostForCbg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 藏宝阁爬虫任务执行器
 * 后台轮询执行任务，支持并发控制、自适应重试和代理池轮换
 */
@Component
public class CbgSpiderTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CbgSpiderTaskExecutor.class);

    // 重试延迟调度器(用于指数退避)
    private ScheduledExecutorService retryScheduler;

    @Autowired
    private CbgSpiderTaskService cbgSpiderTaskService;

    @Autowired
    private CbgAuthConfigService cbgAuthConfigService;

    @Autowired
    private CbgPetListPageProcessor cbgPetListPageProcessor;

    @Autowired
    private CbgPetDetailPageProcessor cbgPetDetailPageProcessor;

    @Value("${spider.task.max-concurrent:3}")
    private int maxConcurrent;

    @Value("${spider.task.poll-interval:5000}")
    private long pollInterval;

    private ExecutorService executorService;
    private volatile boolean isRunning = false;
    
    // 正在重试中的任务集合(防止重复提交重试)
    private final ConcurrentHashMap<Long, Boolean> retryingTasks = new ConcurrentHashMap<>();
    
    // 连续失败计数器（调度启动后累计）
    @Value("${spider.task.max-failures:3}")
    private int maxFailures;
    
    // 当前周期内的失败计数（volatile保证可见性）
    private volatile int consecutiveFailureCount = 0;
    
    // 上一次调度停止原因（用于前端展示）
    private volatile String stopReason = null;

    // 共享下载器实例（多任务复用，避免重复创建Chrome进程）
    private volatile CbgMhxySeleniuDownloader sharedDownloader;
    private volatile String lastAuthConfigHash;  // 用于检测认证配置变化

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(maxConcurrent);
        logger.info("CbgSpiderTaskExecutor 初始化完成，最大并发数: {}, 最大失败次数: {}", maxConcurrent, maxFailures);
    }

    @PreDestroy
    public void destroy() {
        isRunning = false;
        // 关闭共享下载器
        closeSharedDownloader();
        if (executorService != null) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("CbgSpiderTaskExecutor 已关闭");
    }

    /**
     * 后台轮询任务（每5秒执行一次）
     */
    @Scheduled(fixedDelayString = "${spider.task.poll-interval:5000}")
    public void pollTasks() {
        if (!isRunning) {
            return;
        }

        logger.debug("开始轮询任务，调度器状态: isRunning={}", isRunning);

        // 检查认证有效性
        if (!cbgAuthConfigService.checkAuthValid()) {
            logger.warn("认证已失效或未配置，停止执行任务。请先配置认证信息并确保isValid=true");
            return;
        }

        // 获取当前运行中任务数
        int runningCount = cbgSpiderTaskService.getRunningTaskCount();
        logger.debug("当前运行中任务数: {}, 最大并发: {}", runningCount, maxConcurrent);
        
        if (runningCount >= maxConcurrent) {
            logger.debug("已达到最大并发数，跳过本次轮询");
            return;
        }

        // 获取待执行任务
        int availableSlots = maxConcurrent - runningCount;
        List<CbgSpiderTask> pendingTasks = cbgSpiderTaskService.getPendingTasks(availableSlots);
        
        logger.debug("获取到 {} 个待执行任务", pendingTasks.size());

        for (CbgSpiderTask task : pendingTasks) {
            logger.info("提交任务执行: id={}, type={}, url={}", task.getId(), task.getTaskType(), task.getUrl());
            executorService.submit(() -> executeTask(task));
        }
    }

    /**
     * 执行单个任务
     */
    private void executeTask(CbgSpiderTask task) {
        long startTime = System.currentTimeMillis();
        logger.info("[任务{}] 开始执行, type={}, url={}", task.getId(), task.getTaskType(), task.getUrl());
        
        try {
            // 更新任务状态为运行中
            cbgSpiderTaskService.startTask(task.getId());
            logger.info("[任务{}] 状态已更新为RUNNING", task.getId());

            // 获取全局认证配置
            logger.debug("[任务{}] 获取认证配置...", task.getId());
            AppConfigFromPostForCbg appConfig = cbgAuthConfigService.convertToAppConfig();
            logger.debug("[任务{}] 认证配置获取完成, chromeDriverPath={}", task.getId(), appConfig.getChromeDriverPath());

            // 使用共享下载器（多任务复用同一个WebDriver池，避免重复创建Chrome进程）
            CbgMhxySeleniuDownloader downloader = getOrCreateSharedDownloader(appConfig);
            logger.info("[任务{}] 获取共享下载器完成", task.getId());

            // 创建请求
            Request request = new Request(task.getUrl());
            request.putExtra("taskId", task.getId());
            if (task.getExtraData() != null) {
                request.putExtra("extraData", task.getExtraData());
            }

            // 根据任务类型选择处理器
            if ("LIST_PAGE".equals(task.getTaskType())) {
                logger.info("[任务{}] 开始执行列表页爬取: {}", task.getId(), task.getUrl());
                executeListPageTask(request, downloader);
                logger.info("[任务{}] 列表页爬取完成", task.getId());
            } else if ("DETAIL_PAGE".equals(task.getTaskType())) {
                logger.info("[任务{}] 开始执行详情页爬取: {}", task.getId(), task.getUrl());
                executeDetailPageTask(request, downloader);
                logger.info("[任务{}] 详情页爬取完成", task.getId());
            }

            // Spider.run()返回后检查任务状态
            // 注意：WebMagic的Spider会内部catch住Downloader/Processor抛出的异常（仅打日志不重抛），
            // 所以需要通过状态判断来检测是否实际执行成功
            CbgSpiderTask currentTask = cbgSpiderTaskService.getTaskById(task.getId());
            String currentStatus = currentTask != null ? currentTask.getStatus() : null;
            
            if ("FAILED".equals(currentStatus)) {
                // Processor内部已调用failTask标记失败
                handleTaskFailure(task.getId(), "Processor处理失败");
                long duration = System.currentTimeMillis() - startTime;
                logger.warn("[任务{}] Processor已标记为失败，跳过completeTask，耗时{}ms", task.getId(), duration);
            } else if ("SUCCESS".equals(currentStatus)) {
                // Processor内部已调用completeTask标记成功（关联了数据）
                long duration = System.currentTimeMillis() - startTime;
                logger.info("[任务{}] Processor已完成任务(含数据关联)，耗时{}ms", task.getId(), duration);
            } else if ("RUNNING".equals(currentStatus)) {
                // 状态仍为RUNNING说明Spider执行过程中异常被WebMagic吞掉，
                // Processor的completeTask/failTask都未被执行（如认证失效、网络异常等）
                String failReason = "爬取过程中发生异常（如认证失效、页面解析失败或网络超时），未获取到有效数据";
                // 如果是认证相关异常，优先使用更明确的错误信息
                cbgSpiderTaskService.failTask(task.getId(), failReason);
                handleTaskFailure(task.getId(), failReason);
                long duration = System.currentTimeMillis() - startTime;
                logger.error("[任务{}] Spider执行后状态仍为RUNNING，判定为执行失败并标记: {}，耗时{}ms", task.getId(), failReason, duration);
            } else {
                // 其他情况（如STOPPED）：不做额外处理
                long duration = System.currentTimeMillis() - startTime;
                logger.warn("[任务{}] Spider执行后状态为: {}，跳过completeTask，耗时{}ms", task.getId(), currentStatus, duration);
            }

        } catch (AuthInvalidException e) {
            // 认证失效，更新认证状态并停止所有任务
            logger.error("[任务{}] 认证失效: {}", task.getId(), e.getMessage());
            cbgAuthConfigService.updateAuthStatus(false);
            cbgSpiderTaskService.failTask(task.getId(), "认证失效: " + e.getMessage());
            handleTaskFailure(task.getId(), "认证失效: " + e.getMessage());
        } catch (Exception e) {
            logger.error("[任务{}] 执行异常: {}, stack={}", task.getId(), e.getMessage(), e);
            cbgSpiderTaskService.failTask(task.getId(), e.getMessage());
            handleTaskFailure(task.getId(), e.getMessage());
        }
    }

    /**
     * 执行列表页任务
     */
    private void executeListPageTask(Request request, CbgMhxySeleniuDownloader downloader) {
        Spider.create(cbgPetListPageProcessor)
                .addRequest(request)
                .setDownloader(downloader)
                .thread(1)
                .run();
    }

    /**
     * 执行详情页任务
     */
    private void executeDetailPageTask(Request request, CbgMhxySeleniuDownloader downloader) {
        Spider.create(cbgPetDetailPageProcessor)
                .addRequest(request)
                .setDownloader(downloader)
                .thread(1)
                .run();
    }

    /**
     * 手动触发执行任务
     */
    public void executeTaskManually(Long taskId) {
        CbgSpiderTask task = cbgSpiderTaskService.getTaskById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不是待执行: " + task.getStatus());
        }
        executorService.submit(() -> executeTask(task));
    }

    /**
     * 启动调度器
     */
    public void startScheduler() {
        isRunning = true;
        // 重置失败计数器（新的统计周期开始）
        consecutiveFailureCount = 0;
        stopReason = null;
        // 重置共享下载器，确保使用最新的认证配置
        closeSharedDownloader();
        logger.info("调度器已启动，开始轮询任务（失败计数已重置，阈值: {}次）", maxFailures);
    }

    /**
     * 停止调度器
     */
    public void stopScheduler() {
        isRunning = false;
        logger.info("调度器已停止");
    }
    
    /**
     * 处理任务失败：增加失败计数，达到阈值自动停止调度
     */
    private synchronized void handleTaskFailure(Long taskId, String reason) {
        consecutiveFailureCount++;
        logger.warn("[任务{}] 执行失败, 当前连续失败次数: {}/{}, 原因: {}", taskId, consecutiveFailureCount, maxFailures, reason);
        
        if (consecutiveFailureCount >= maxFailures) {
            stopReason = "连续失败" + consecutiveFailureCount + "次，已达阈值(" + maxFailures + "次)，自动停止调度。最后失败原因: " + reason;
            logger.error(stopReason);
            isRunning = false;
            logger.warn("========== 调度器已因连续失败达到阈值而自动停止 ==========");
        }
    }

    /**
     * 是否运行中
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 获取当前连续失败次数
     */
    public int getConsecutiveFailureCount() {
        return consecutiveFailureCount;
    }
    
    /**
     * 获取最大失败次数阈值
     */
    public int getMaxFailures() {
        return maxFailures;
    }
    
    /**
     * 获取停止原因
     */
    public String getStopReason() {
        return stopReason;
    }

    /**
     * 获取或创建共享下载器（多任务复用）
     * 使用双重检查锁定保证线程安全，认证配置变化时自动重建
     */
    private CbgMhxySeleniuDownloader getOrCreateSharedDownloader(AppConfigFromPostForCbg appConfig) {
        // 计算当前配置的hash，用于检测变化
        String currentHash = computeAuthConfigHash(appConfig);
        
        if (sharedDownloader == null) {
            synchronized (this) {
                if (sharedDownloader == null) {
                    sharedDownloader = createNewDownloader(appConfig);
                    lastAuthConfigHash = currentHash;
                    logger.info("创建新的共享下载器实例");
                    return sharedDownloader;
                }
            }
        }

        // 检测认证配置是否发生变化（如用户更新了sid等）
        if (!currentHash.equals(lastAuthConfigHash)) {
            synchronized (this) {
                // 双重检查
                if (!currentHash.equals(lastAuthConfigHash)) {
                    logger.info("检测到认证配置变化，重建共享下载器...");
                    closeSharedDownloaderInternal();
                    sharedDownloader = createNewDownloader(appConfig);
                    lastAuthConfigHash = currentHash;
                }
            }
        }

        return sharedDownloader;
    }

    /**
     * 创建新的下载器实例
     */
    private CbgMhxySeleniuDownloader createNewDownloader(AppConfigFromPostForCbg appConfig) {
        System.getProperties().setProperty("webdriver.chrome.driver", appConfig.getChromeDriverPath());
        // 设置selenium配置文件路径
        if (appConfig.getSelenuimConfig() != null) {
            System.setProperty("selenuim_config", appConfig.getSelenuimConfig());
        }
        // 设置headless模式
        if (appConfig.isHeadlessMode()) {
            System.setProperty("headless", "true");
        } else {
            System.setProperty("headless", "false");
        }
        return new CbgMhxySeleniuDownloader(appConfig.getChromeDriverPath(), appConfig);
    }

    /**
     * 关闭共享下载器（外部调用）
     */
    private void closeSharedDownloader() {
        synchronized (this) {
            closeSharedDownloaderInternal();
        }
    }

    /**
     * 内部关闭共享下载器（不加锁版本）
     */
    private void closeSharedDownloaderInternal() {
        if (sharedDownloader != null) {
            try {
                logger.info("关闭共享下载器及WebDriver池...");
                sharedDownloader.close();
                logger.info("共享下载器已关闭");
            } catch (Exception e) {
                logger.warn("关闭共享下载器时异常: {}", e.getMessage());
            } finally {
                sharedDownloader = null;
                lastAuthConfigHash = null;
            }
        }
    }

    /**
     * 计算认证配置的简单哈希值，用于检测配置变更
     */
    private String computeAuthConfigHash(AppConfigFromPostForCbg config) {
        if (config == null) return "null";
        return String.valueOf(Objects.hash(
                config.getSid(),
                config.getLogin_id(),
                config.getCbg_qrcode(),
                config.getReco_sid(),
                config.getChromeDriverPath(),
                config.getSelenuimConfig()
        ));
    }
}
