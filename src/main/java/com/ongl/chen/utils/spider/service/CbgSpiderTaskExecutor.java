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

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(maxConcurrent);
        logger.info("CbgSpiderTaskExecutor 初始化完成，最大并发数: {}", maxConcurrent);
    }

    @PreDestroy
    public void destroy() {
        isRunning = false;
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

            // 创建下载器
            logger.info("[任务{}] 创建下载器...", task.getId());
            CbgMhxySeleniuDownloader downloader = new CbgMhxySeleniuDownloader(
                    appConfig.getChromeDriverPath(), appConfig);
            logger.info("[任务{}] 下载器创建完成", task.getId());

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

            // 任务执行完成
            cbgSpiderTaskService.completeTask(task.getId(), null);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[任务{}] 执行完成, 耗时{}ms", task.getId(), duration);

        } catch (AuthInvalidException e) {
            // 认证失效，更新认证状态并停止所有任务
            logger.error("[任务{}] 认证失效: {}", task.getId(), e.getMessage());
            cbgAuthConfigService.updateAuthStatus(false);
            cbgSpiderTaskService.failTask(task.getId(), "认证失效: " + e.getMessage());
        } catch (Exception e) {
            logger.error("[任务{}] 执行异常: {}, stack={}", task.getId(), e.getMessage(), e);
            cbgSpiderTaskService.failTask(task.getId(), e.getMessage());
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
        logger.info("调度器已启动，开始轮询任务");
    }

    /**
     * 停止调度器
     */
    public void stopScheduler() {
        isRunning = false;
        logger.info("调度器已停止");
    }

    /**
     * 是否运行中
     */
    public boolean isRunning() {
        return isRunning;
    }
}
