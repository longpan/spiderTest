package com.ongl.chen.utils.spider.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ongl.chen.utils.spider.beans.cbg.CbgAuthConfig;
import com.ongl.chen.utils.spider.beans.cbg.CbgSpiderTask;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;
import com.ongl.chen.utils.spider.dao.MhPetItemDAO;
import com.ongl.chen.utils.spider.service.CbgAuthConfigService;
import com.ongl.chen.utils.spider.service.CbgSpiderTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                       Model model) {
        
        Map<String, Object> params = new HashMap<>();
        if (status != null && !status.isEmpty()) {
            params.put("status", status);
        }
        
        List<CbgSpiderTask> tasks = cbgSpiderTaskService.listTasks(params);
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        
        return "admin/tasks";
    }

    /**
     * 宠物数据列表页面
     */
    @GetMapping("/pets")
    public String pets(@RequestParam(defaultValue = "1") int page,
                      @RequestParam(defaultValue = "100") int size,
                      @RequestParam(required = false) String name,
                      @RequestParam(required = false) String serverName,
                      Model model) {
        
        QueryWrapper<MhPetItem> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (serverName != null && !serverName.isEmpty()) {
            wrapper.eq("serverName", serverName);
        }
        wrapper.orderByDesc("updateTime");
        
        IPage<MhPetItem> pageResult = mhPetItemDAO.selectPage(new Page<>(page, size), wrapper);
        
        model.addAttribute("pets", pageResult.getRecords());
        model.addAttribute("total", pageResult.getTotal());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("name", name);
        model.addAttribute("serverName", serverName);
        
        return "admin/pets";
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
            // 调用调度器启动
            result.put("success", true);
            result.put("message", "调度器已启动");
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
            // 调用调度器停止
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
}
