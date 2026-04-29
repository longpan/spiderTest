package com.ongl.chen.utils.spider.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ongl.chen.utils.spider.beans.cbg.CbgAuthConfig;
import com.ongl.chen.utils.spider.beans.cbg.CbgSpiderTask;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;
import com.ongl.chen.utils.spider.dao.MhPetItemDAO;
import com.ongl.chen.utils.spider.service.CbgAuthConfigService;
import com.ongl.chen.utils.spider.service.CbgSpiderTaskExecutor;
import com.ongl.chen.utils.spider.service.CbgSpiderTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * 管理页面Controller
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CbgSpiderTaskService cbgSpiderTaskService;

    @Autowired
    private CbgAuthConfigService cbgAuthConfigService;

    @Autowired
    private MhPetItemDAO mhPetItemDAO;

    @Autowired
    private CbgSpiderTaskExecutor cbgSpiderTaskExecutor;

    /**
     * 管理首页
     */
    @GetMapping("")
    public String index() {
        return "redirect:/admin/dashboard";
    }

    /**
     * 仪表盘页面
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 统计数据
        Map<String, Object> stats = new HashMap<>();
        
        // 任务统计
        List<CbgSpiderTask> allTasks = cbgSpiderTaskService.listTasks(null);
        long pendingCount = allTasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count();
        long runningCount = allTasks.stream().filter(t -> "RUNNING".equals(t.getStatus())).count();
        long successCount = allTasks.stream().filter(t -> "SUCCESS".equals(t.getStatus())).count();
        long failedCount = allTasks.stream().filter(t -> "FAILED".equals(t.getStatus())).count();
        
        stats.put("totalTasks", allTasks.size());
        stats.put("pendingCount", pendingCount);
        stats.put("runningCount", runningCount);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        
        // 宠物数据统计
        Integer petCount = mhPetItemDAO.selectCount(new QueryWrapper<>());
        stats.put("petCount", petCount);
        
        // 认证状态
        CbgAuthConfig authConfig = cbgAuthConfigService.getGlobalConfig();
        model.addAttribute("authConfig", authConfig);
        
        model.addAttribute("stats", stats);
        model.addAttribute("recentTasks", allTasks.stream().limit(10).collect(Collectors.toList()));
        
        return "admin/dashboard";
    }

    /**
     * 任务列表页面
     */
    @GetMapping("/tasks")
    public String tasks(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String taskType,
                       @RequestParam(defaultValue = "createTime") String sortField,
                       @RequestParam(defaultValue = "desc") String sortOrder,
                       Model model) {
        
        // 验证排序字段
        String validSortField = validateTaskSortField(sortField);
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        if (status != null && !status.isEmpty()) {
            params.put("status", status);
        }
        if (taskType != null && !taskType.isEmpty()) {
            params.put("taskType", taskType);
        }
        params.put("sortField", validSortField);
        params.put("sortOrder", isAsc ? "asc" : "desc");
        params.put("page", page);
        params.put("size", size);
        
        // 分页查询
        IPage<CbgSpiderTask> pageResult = cbgSpiderTaskService.listTasksPage(params);
        
        long totalPages = (pageResult.getTotal() + size - 1) / size;
        
        // 统计数据（用于任务数量卡片）
        Map<String, Object> stats = new HashMap<>();
        List<CbgSpiderTask> allTasks = cbgSpiderTaskService.listTasks(null);
        stats.put("totalTasks", allTasks.size());
        stats.put("pendingCount", allTasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count());
        stats.put("runningCount", allTasks.stream().filter(t -> "RUNNING".equals(t.getStatus())).count());
        stats.put("successCount", allTasks.stream().filter(t -> "SUCCESS".equals(t.getStatus())).count());
        stats.put("failedCount", allTasks.stream().filter(t -> "FAILED".equals(t.getStatus())).count());
        Integer petCount = mhPetItemDAO.selectCount(new QueryWrapper<>());
        stats.put("petCount", petCount);
        model.addAttribute("stats", stats);
        
        model.addAttribute("tasks", pageResult.getRecords());
        model.addAttribute("total", pageResult.getTotal());
        model.addAttribute("totalPages", totalPages > 0 ? totalPages : 1);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("status", status);
        model.addAttribute("taskType", taskType);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortOrder", sortOrder);
        
        return "admin/tasks";
    }
    
    /**
     * 验证任务排序字段
     */
    private String validateTaskSortField(String sortField) {
        Set<String> validFields = new HashSet<>(Arrays.asList(
                "createTime", "id", "status", "taskType", "retryCount"
        ));
        return validFields.contains(sortField) ? sortField : "createTime";
    }

    /**
     * 宠物数据列表页面
     */
    @GetMapping("/pets")
    public String pets(@RequestParam(defaultValue = "1") int page,
                      @RequestParam(defaultValue = "100") int size,
                      @RequestParam(required = false) String name,
                      @RequestParam(required = false) String serverName,
                      @RequestParam(defaultValue = "updateTime") String sortField,
                      @RequestParam(defaultValue = "desc") String sortOrder,
                      Model model) {
        
        QueryWrapper<MhPetItem> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (serverName != null && !serverName.isEmpty()) {
            wrapper.eq("serverName", serverName);
        }
        
        // 验证排序字段，防止SQL注入
        String validSortField = validateSortField(sortField);
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        
        if (isAsc) {
            wrapper.orderByAsc(validSortField);
        } else {
            wrapper.orderByDesc(validSortField);
        }
        
        IPage<MhPetItem> pageResult = mhPetItemDAO.selectPage(new Page<>(page, size), wrapper);
        
        long totalPages = (pageResult.getTotal() + size - 1) / size;
        
        // 查询可选的宠物名称列表（按数量排序，前50个）
        List<Map<String, Object>> petNameCountList = mhPetItemDAO.selectMaps(
                new QueryWrapper<MhPetItem>()
                        .select("name", "COUNT(*) as count")
                        .isNotNull("name")
                        .ne("name", "")
                        .groupBy("name")
                        .orderByDesc("count")
                        .last("LIMIT 50")
        );
        List<Map<String, Object>> petNames = petNameCountList.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", m.get("name"));
                    map.put("count", m.get("count"));
                    return map;
                })
                .collect(Collectors.toList());
        
        // 查询可选的服务器列表（去重）
        QueryWrapper<MhPetItem> serverWrapper = new QueryWrapper<>();
        serverWrapper.select("DISTINCT serverName").isNotNull("serverName").ne("serverName", "").orderByAsc("serverName");
        List<String> serverNames = mhPetItemDAO.selectObjs(serverWrapper).stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        
        model.addAttribute("pets", pageResult.getRecords());
        model.addAttribute("total", pageResult.getTotal());
        model.addAttribute("totalPages", totalPages > 0 ? totalPages : 1);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("name", name);
        model.addAttribute("serverName", serverName);
        model.addAttribute("petNames", petNames);
        model.addAttribute("serverNames", serverNames);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortOrder", sortOrder);
        
        return "admin/pets";
    }
    
    /**
     * 验证排序字段，防止SQL注入
     */
    private String validateSortField(String sortField) {
        Set<String> validFields = new HashSet<>(Arrays.asList(
                "price", "collect", "skillNum", "updateTime", "id", "name", "level", "code",
                "valuationValue", "valuationScore"
        ));
        return validFields.contains(sortField) ? sortField : "updateTime";
    }

    /**
     * 认证配置页面
     */
    @GetMapping("/auth")
    public String auth(Model model) {
        CbgAuthConfig authConfig = cbgAuthConfigService.getGlobalConfig();
        model.addAttribute("authConfig", authConfig);
        return "admin/auth";
    }

    /**
     * 日志查看页面
     */
    @GetMapping("/logs")
    public String logs() {
        return "admin/logs";
    }

    // ============ API接口 ============

    /**
     * 创建列表页爬取任务
     */
    @PostMapping("/api/task/create/list")
    @ResponseBody
    public Map<String, Object> createListTask(@RequestParam String url, @RequestParam(defaultValue = "1") int maxPage) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (url == null || url.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "URL不能为空");
                return result;
            }
            cbgSpiderTaskService.createListPageTasks(url.trim(), maxPage);
            result.put("success", true);
            result.put("message", "列表页任务创建成功,共创建 " + maxPage + " 个页任务");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 创建详情页爬取任务
     */
    @PostMapping("/api/task/create/detail")
    @ResponseBody
    public Map<String, Object> createDetailTask(@RequestParam String url, 
                                                  @RequestParam(required = false) String itemCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (url == null || url.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "URL不能为空");
                return result;
            }
            
            Map<String, Object> extras = new HashMap<>();
            extras.put("detailUrl", url.trim());
            if (itemCode != null && !itemCode.isEmpty()) {
                extras.put("itemCode", itemCode.trim());
            }
            
            cbgSpiderTaskService.createDetailPageTask(null, url.trim(), extras.size() > 0 ? extras : null);
            result.put("success", true);
            result.put("message", "详情页任务创建成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新认证配置
     */
    @PostMapping("/api/auth")
    @ResponseBody
    public Map<String, Object> updateAuth(@RequestBody CbgAuthConfig authConfig) {
        Map<String, Object> result = new HashMap<>();
        try {
            cbgAuthConfigService.saveOrUpdateConfig(authConfig);
            result.put("success", true);
            result.put("message", "认证配置已更新");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 启动调度器
     */
    @PostMapping("/api/scheduler/start")
    @ResponseBody
    public Map<String, Object> startScheduler() {
        Map<String, Object> result = new HashMap<>();
        try {
            cbgSpiderTaskExecutor.startScheduler();
            result.put("success", true);
            result.put("message", "调度器已启动（失败阈值: " + cbgSpiderTaskExecutor.getMaxFailures() + "次）");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "启动失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 停止调度器
     */
    @PostMapping("/api/scheduler/stop")
    @ResponseBody
    public Map<String, Object> stopScheduler() {
        Map<String, Object> result = new HashMap<>();
        try {
            cbgSpiderTaskExecutor.stopScheduler();
            result.put("success", true);
            result.put("message", "调度器已停止");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "停止失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 重试任务
     */
    @PostMapping("/api/task/{taskId}/retry")
    @ResponseBody
    public Map<String, Object> retryTask(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            cbgSpiderTaskService.retryTask(taskId);
            result.put("success", true);
            result.put("message", "任务已重新加入队列");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重试失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 停止任务
     */
    @PostMapping("/api/task/{taskId}/stop")
    @ResponseBody
    public Map<String, Object> stopTask(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            cbgSpiderTaskService.stopTask(taskId);
            result.put("success", true);
            result.put("message", "任务已停止");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "停止失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 创建重新爬取任务
     */
    @PostMapping("/api/task/replay")
    @ResponseBody
    public Map<String, Object> createReplayTask(@RequestParam String itemCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            cbgSpiderTaskService.createReplayTaskByItemCode(itemCode);
            result.put("success", true);
            result.put("message", "重新爬取任务已创建");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/api/task/{taskId}")
    @ResponseBody
    public Map<String, Object> getTaskDetail(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        CbgSpiderTask task = cbgSpiderTaskService.getTaskById(taskId);
        if (task != null) {
            result.put("success", true);
            result.put("data", task);
        } else {
            result.put("success", false);
            result.put("message", "任务不存在");
        }
        return result;
    }

    /**
     * 获取宠物详情
     */
    @GetMapping("/api/pet/{petId}")
    @ResponseBody
    public Map<String, Object> getPetDetail(@PathVariable Long petId) {
        Map<String, Object> result = new HashMap<>();
        MhPetItem pet = mhPetItemDAO.selectById(petId);
        if (pet != null) {
            result.put("success", true);
            result.put("data", pet);
        } else {
            result.put("success", false);
            result.put("message", "宠物数据不存在");
        }
        return result;
    }

    /**
     * 导出选中的宠物数据
     */
    @PostMapping("/api/pets/export-selected")
    public void exportSelectedPets(@RequestBody Map<String, List<Long>> requestBody, HttpServletResponse response) throws IOException {
        List<Long> ids = requestBody.get("ids");
        if (ids == null || ids.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("请选择要导出的数据");
            return;
        }
        
        // 查询数据
        List<MhPetItem> pets = mhPetItemDAO.selectBatchIds(ids);
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("宠物数据_选中_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 导出Excel
        EasyExcel.write(response.getOutputStream(), MhPetItem.class)
                .sheet("宠物数据")
                .doWrite(pets);
    }

    /**
     * 导出所有宠物数据
     */
    @GetMapping("/api/pets/export-all")
    public void exportAllPets(HttpServletResponse response) throws IOException {
        // 查询所有数据
        List<MhPetItem> pets = mhPetItemDAO.selectList(null);
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("宠物数据_全部_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 导出Excel
        EasyExcel.write(response.getOutputStream(), MhPetItem.class)
                .sheet("宠物数据")
                .doWrite(pets);
    }

    /**
     * 批量删除任务
     */
    @PostMapping("/api/task/batch-delete")
    @ResponseBody
    public Map<String, Object> batchDeleteTasks(@RequestBody Map<String, List<Long>> requestBody) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> taskIds = requestBody.get("taskIds");
            if (taskIds == null || taskIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要删除的任务");
                return result;
            }
            
            // 检查是否有运行中的任务
            for (Long taskId : taskIds) {
                CbgSpiderTask task = cbgSpiderTaskService.getTaskById(taskId);
                if (task != null && "RUNNING".equals(task.getStatus())) {
                    result.put("success", false);
                    result.put("message", "无法删除运行中的任务，请先停止: 任务ID=" + taskId);
                    return result;
                }
            }
            
            int deletedCount = cbgSpiderTaskService.batchDeleteTasks(taskIds);
            result.put("success", true);
            result.put("message", "成功删除 " + deletedCount + " 个任务");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 批量重试任务
     */
    @PostMapping("/api/task/batch-retry")
    @ResponseBody
    public Map<String, Object> batchRetryTasks(@RequestBody Map<String, List<Long>> requestBody) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> taskIds = requestBody.get("taskIds");
            if (taskIds == null || taskIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要重试的任务");
                return result;
            }
            
            // 检查是否有运行中的任务
            for (Long taskId : taskIds) {
                CbgSpiderTask task = cbgSpiderTaskService.getTaskById(taskId);
                if (task != null && "RUNNING".equals(task.getStatus())) {
                    result.put("success", false);
                    result.put("message", "无法重试运行中的任务，请先停止: 任务ID=" + taskId);
                    return result;
                }
            }
            
            int retriedCount = cbgSpiderTaskService.batchRetryTasks(taskIds);
            result.put("success", true);
            result.put("message", "成功重试 " + retriedCount + " 个任务（仅失败/停止状态的任务会被重试）");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重试失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 重新运行任务（成功状态也可）
     */
    @PostMapping("/api/task/{taskId}/rerun")
    @ResponseBody
    public Map<String, Object> reRunTask(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            CbgSpiderTask task = cbgSpiderTaskService.getTaskById(taskId);
            if (task == null) {
                result.put("success", false);
                result.put("message", "任务不存在");
                return result;
            }
            if ("RUNNING".equals(task.getStatus())) {
                result.put("success", false);
                result.put("message", "无法重新运行正在执行中的任务，请先停止");
                return result;
            }
            cbgSpiderTaskService.reRunTask(taskId);
            result.put("success", true);
            result.put("message", "任务已重新加入队列执行");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 批量重新运行任务
     */
    @PostMapping("/api/task/batch-rerun")
    @ResponseBody
    public Map<String, Object> batchReRunTasks(@RequestBody Map<String, List<Long>> requestBody) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> taskIds = requestBody.get("taskIds");
            if (taskIds == null || taskIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要重新运行的任务");
                return result;
            }
            
            // 检查是否有运行中的任务
            for (Long taskId : taskIds) {
                CbgSpiderTask task = cbgSpiderTaskService.getTaskById(taskId);
                if (task != null && "RUNNING".equals(task.getStatus())) {
                    result.put("success", false);
                    result.put("message", "无法重新运行正在执行中的任务，请先停止: 任务ID=" + taskId);
                    return result;
                }
            }
            
            int rerunCount = cbgSpiderTaskService.batchReRunTasks(taskIds);
            result.put("success", true);
            result.put("message", "成功将 " + rerunCount + " 个任务重新加入队列（已排除运行中任务）");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }
        return result;
    }

    // ============ 日志管理API ============

    /**
     * 重新抓取选中的宠物
     */
    @PostMapping("/api/pets/rerun-selected")
    @ResponseBody
    public Map<String, Object> rerunSelectedPets(@RequestBody Map<String, List<Long>> requestBody) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> ids = requestBody.get("ids");
            if (ids == null || ids.isEmpty()) {
                result.put("success", false);
                result.put("message", "请至少选择一条数据");
                return result;
            }
            
            // 获取选中的宠物
            List<MhPetItem> pets = mhPetItemDAO.selectBatchIds(ids);
            int successCount = 0;
            int failCount = 0;
            
            for (MhPetItem pet : pets) {
                if (pet.getDetailUrl() != null && !pet.getDetailUrl().trim().isEmpty()) {
                    boolean rerunSuccess = cbgSpiderTaskService.reRunTaskByUrl(pet.getDetailUrl());
                    if (rerunSuccess) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } else {
                    failCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", "成功创建/重置 " + successCount + " 个任务，失败 " + failCount + " 个");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 重新抓取所有宠物
     */
    @PostMapping("/api/pets/rerun-all")
    @ResponseBody
    public Map<String, Object> rerunAllPets() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有宠物
            List<MhPetItem> pets = mhPetItemDAO.selectList(null);
            int successCount = 0;
            int failCount = 0;
            
            for (MhPetItem pet : pets) {
                if (pet.getDetailUrl() != null && !pet.getDetailUrl().trim().isEmpty()) {
                    boolean rerunSuccess = cbgSpiderTaskService.reRunTaskByUrl(pet.getDetailUrl());
                    if (rerunSuccess) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } else {
                    failCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", "成功创建/重置 " + successCount + " 个任务，失败 " + failCount + " 个");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }
        return result;
    }

    private static final String LOG_DIR = System.getProperty("user.dir") + "/logs";
    private static final String LOG_FILE_PREFIX = "spider.log";

    /**
     * 获取日志内容（支持分页和搜索）
     */
    @GetMapping("/api/logs")
    @ResponseBody
    public Map<String, Object> getLogs(
            @RequestParam(defaultValue = "100") int lines,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "") String date) {
        Map<String, Object> result = new HashMap<>();
        List<String> logLines = new ArrayList<>();
        int totalLines = 0;
        
        try {
            Path logPath = resolveLogFilePath(date);
            System.out.println("[AdminController] 日志路径: " + logPath + ", 存在: " + (logPath != null ? Files.exists(logPath) : "null"));
            
            if (logPath != null && Files.exists(logPath)) {
                // 倒读文件获取最新的日志
                try (RandomAccessFile raf = new RandomAccessFile(logPath.toFile(), "r")) {
                    long fileLength = raf.length();
                    long pos = fileLength - 1;
                    StringBuilder sb = new StringBuilder();
                    
                    while (pos >= 0 && logLines.size() < lines * 3) { // 多读一些用于过滤
                        raf.seek(pos);
                        int ch = raf.read();
                        if (ch == '\n' || ch == '\r') {
                            if (sb.length() > 0) {
                                String line = sb.reverse().toString();
                                totalLines++;
                                if (matchLine(line, keyword, level)) {
                                    logLines.add(line);
                                }
                                sb = new StringBuilder();
                            }
                        } else if (pos == 0) {
                            sb.append((char) ch);
                            String line = sb.reverse().toString();
                            totalLines++;
                            if (matchLine(line, keyword, level)) {
                                logLines.add(line);
                            }
                        } else {
                            sb.append((char) ch);
                        }
                        pos--;
                    }
                }
                
                Collections.reverse(logLines);
                if (logLines.size() > lines) {
                    logLines = logLines.subList(0, lines);
                }
            }
            
            result.put("success", true);
            result.put("data", logLines);
            result.put("totalLines", totalLines);
            result.put("logFile", logPath != null ? logPath.getFileName().toString() : null);
            result.put("lines", logLines.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "读取日志失败: " + e.getMessage());
            result.put("data", Collections.emptyList());
        }
        
        return result;
    }

    /**
     * 获取可用日志日期列表
     */
    @GetMapping("/api/logs/dates")
    @ResponseBody
    public Map<String, Object> getLogDates() {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        
        try {
            Path dir = Paths.get(LOG_DIR);
            if (Files.isDirectory(dir)) {
                Files.list(dir)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith(LOG_FILE_PREFIX) && !name.endsWith(".lck");
                    })
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> dates.add(extractDateFromFileName(p.getFileName().toString())));
            } else {
                System.out.println("[AdminController] 日志目录不存在: " + dir.toAbsolutePath());
            }
            
            result.put("success", true);
            result.put("dates", dates);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取日志列表失败: " + e.getMessage());
            result.put("dates", Collections.emptyList());
        }
        
        return result;
    }

    private Path resolveLogFilePath(String date) {
        String fileName = (date != null && !date.isEmpty()) ? LOG_FILE_PREFIX + "." + date : LOG_FILE_PREFIX;
        Path path = Paths.get(LOG_DIR, fileName);
        if (existsAndNotEmpty(path)) return path;

        // 扫描日志目录，找到最新的匹配文件
        Path dirPath = Paths.get(LOG_DIR);
        try {
            if (Files.isDirectory(dirPath)) {
                List<Path> logFiles = new ArrayList<>();
                java.util.stream.Stream<Path> stream = Files.list(dirPath);
                stream.filter(p -> p.getFileName().toString().startsWith(LOG_FILE_PREFIX))
                    .filter(p -> !p.getFileName().toString().endsWith(".lck"))
                    .sorted((a, b) -> { try { return Long.compare(Files.getLastModifiedTime(b).toMillis(), Files.getLastModifiedTime(a).toMillis()); } catch(Exception e) { return 0; } })
                    .forEach(logFiles::add);
                stream.close();

                // 日期精确匹配
                if (date != null && !date.isEmpty()) {
                    for (Path f : logFiles) {
                        if (f.getFileName().toString().contains(date) && existsAndNotEmpty(f)) return f;
                    }
                }
                // 返回最新
                for (Path f : logFiles) {
                    if (existsAndNotEmpty(f)) return f;
                }
            }
        } catch (Exception e) { /* ignore */ }

        // 兜底：spring.log
        Path springLog = Paths.get(LOG_DIR, "spring.log");
        if (existsAndNotEmpty(springLog)) return springLog;

        System.out.println("[AdminController] 未找到日志文件, LOG_DIR=" + LOG_DIR);
        return null;
    }

    private boolean existsAndNotEmpty(Path path) {
        try { return Files.exists(path) && Files.size(path) > 0; } catch (Exception e) { return false; }
    }

    private String extractDateFromFileName(String fileName) {
        if (fileName.equals(LOG_FILE_PREFIX)) return "";
        if (fileName.startsWith(LOG_FILE_PREFIX + ".")) {
            return fileName.substring((LOG_FILE_PREFIX + ".").length());
        }
        return fileName;
    }

    private boolean matchLine(String line, String keyword, String level) {
        if (keyword != null && !keyword.isEmpty() && !line.toLowerCase().contains(keyword.toLowerCase())) {
            return false;
        }
        if (level != null && !level.isEmpty()) {
            // 日志格式：2026-04-13 08:47:28 INFO ... 
            // level在时间戳后面
            int timeEnd = line.indexOf(' ', 19); // 跳过日期+空格后的第一个空格
            if (timeEnd > 0 && timeEnd + 1 < line.length()) {
                String lineLevel = line.substring(timeEnd + 1).split("\\s+")[0];
                if (!lineLevel.equalsIgnoreCase(level)) {
                    return false;
                }
            }
        }
        return true;
    }
}
