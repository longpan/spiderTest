package com.ongl.chen.utils.spider.controller;

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

import java.util.*;
import java.util.stream.Collectors;

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
                "price", "collect", "skillNum", "updateTime", "id", "name", "level", "code"
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
}
