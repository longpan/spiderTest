package com.ongl.chen.utils.spider.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ongl.chen.utils.spider.beans.CbgItem;
import com.ongl.chen.utils.spider.beans.cbg.CbgAuthConfig;
import com.ongl.chen.utils.spider.beans.cbg.CbgSpiderTask;
import com.ongl.chen.utils.spider.dao.CbgItemDAO;
import com.ongl.chen.utils.spider.pipline.CbgItemExcelPipline;
import com.ongl.chen.utils.spider.processor.*;
import com.ongl.chen.utils.spider.processor.cbg.CbgMhxyEquipProcessor;
import com.ongl.chen.utils.spider.processor.cbg.CbgMhxyLingShiProcessor;
import com.ongl.chen.utils.spider.processor.cbg.CbgMhxyProcessor;
import com.ongl.chen.utils.spider.service.*;
import com.ongl.chen.utils.spider.utils.AppConfigFromPost;
import com.ongl.chen.utils.spider.utils.AppConfigFromPostForCbg;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "藏宝阁爬虫接口", description = "爬虫任务管理、认证配置、数据爬取相关接口")
public class RunnerController {

    @Autowired
    private JDProductProcessor jdProductProcessor;

    @Autowired
    private GXRCWProcessor gxrcwProcessor;

    @Autowired
    private CbgMhxysyProcessorV5 cbgMhxysyProcessorV5;

    @Autowired
    private CbgItemDAO cbgItemDAO;

    @Autowired
    private CbgItemService cbgItemService;

    @Autowired
    private CbgMhxyProcessor cbgMhxyProcessor;

    @Autowired
    private CbgMhxyLingShiProcessor cbgMhxyLingShiProcessor;

    @Autowired
    private CbgMhxyEquipProcessor cbgMhxyEquipProcessor;

    @Autowired
    MhPetItemService mhPetItemService;

    @Autowired
    MhLingShiItemService mhLingShiItemService;

    @Autowired
    MhEquipItemService mhEquipItemService;

    @Autowired
    private CbgAuthConfigService cbgAuthConfigService;

    @Autowired
    private CbgSpiderTaskService cbgSpiderTaskService;

    @Autowired
    private CbgSpiderTaskExecutor cbgSpiderTaskExecutor;

    @GetMapping("/gxrcw")
    public void gxrcw(@RequestParam(required = true) String keyWord) {

        gxrcwProcessor.start(keyWord);
    }

    @GetMapping("/jd")
    public void jd(@RequestParam(required = true) String keyWord) {

        jdProductProcessor.start(keyWord);
    }

    @PostMapping("/cgb")
    public void cbg(@RequestBody AppConfigFromPost appConfigFromPost) throws InterruptedException {
        int cycleIndex = 1;
        while (true) {
            try {
                System.out.println("cbgMhxysyProcessorV5.start times: " + cycleIndex);
                cbgMhxysyProcessorV5.start(appConfigFromPost, cbgItemService);
                Thread.sleep(60*1000*5);
            }catch (Exception e){
                System.out.println("RunnerController Exception, errMsg: " + e.getMessage());
            }


        }

    }

    @GetMapping("/test")
    public void test() {
        CbgItem cbgItem = new CbgItem();
        cbgItem.setCode("xxx");
//        cbgItemDAO.insert(cbgItem);
        cbgItemService.insertByCode(cbgItem);
    }

    @GetMapping("/exportCbg")
    public void exportCbg() {
        CbgItem cbgItem = new CbgItem();
        cbgItem.setCode("xxx");
        List<CbgItem> cbgItemList =   cbgItemDAO.selectList(new QueryWrapper<>());
        CbgItemExcelPipline.saveExcel(cbgItemList);
//        cbgItemDAO.insert(cbgItem);
       // cbgItemService.insertByCode(cbgItem);
    }

    @PostMapping("/cgbPet")
    public void cbgPet(@RequestBody AppConfigFromPostForCbg appConfigFromPost) throws InterruptedException {
        cbgMhxyProcessor.start(appConfigFromPost, mhPetItemService);
    }

    @PostMapping("/stopPet")
    public void stopPet() {
        cbgMhxyProcessor.stop();
    }
    @PostMapping("/cgbLingShi")
    public void cbgLingShi(@RequestBody AppConfigFromPostForCbg appConfigFromPost) throws InterruptedException {
        cbgMhxyLingShiProcessor.start(appConfigFromPost, mhLingShiItemService);
    }

    @PostMapping("/cgbEquip")
    public void cbgEquip(@RequestBody AppConfigFromPostForCbg appConfigFromPost) throws InterruptedException {
        cbgMhxyEquipProcessor.start(appConfigFromPost, mhEquipItemService);
    }

    // ==================== 认证配置管理接口 ====================

    /**
     * 保存全局认证配置
     */
    @PostMapping("/auth/config")
    @ApiOperation(value = "保存全局认证配置", notes = "保存或更新全局认证配置信息，固定id=1")
    public String saveAuthConfig(@RequestBody CbgAuthConfig config) {
        cbgAuthConfigService.saveOrUpdateConfig(config);
        return "认证配置保存成功";
    }

    /**
     * 获取当前认证配置
     */
    @GetMapping("/auth/config")
    @ApiOperation(value = "获取当前认证配置", notes = "获取全局认证配置信息")
    public CbgAuthConfig getAuthConfig() {
        return cbgAuthConfigService.getGlobalConfig();
    }

    /**
     * 检查认证是否有效
     */
    @PostMapping("/auth/config/check")
    @ApiOperation(value = "检查认证是否有效", notes = "检查当前认证配置是否有效")
    public boolean checkAuthValid() {
        return cbgAuthConfigService.checkAuthValid();
    }

    /**
     * 手动设置认证状态
     */
    @PostMapping("/auth/config/status")
    @ApiOperation(value = "手动设置认证状态", notes = "手动设置认证状态为有效或失效")
    public String updateAuthStatus(@ApiParam("是否有效") @RequestParam boolean isValid) {
        cbgAuthConfigService.updateAuthStatus(isValid);
        return "认证状态已更新: " + isValid;
    }

    // ==================== 任务管理接口 ====================

    /**
     * 创建爬取任务
     */
    @PostMapping("/task/create")
    @ApiOperation(value = "创建爬取任务", notes = "创建列表页爬取任务")
    public String createTask(@ApiParam("爬取URL") @RequestParam String url, 
                             @ApiParam("最大页数") @RequestParam int maxPage) {
        cbgSpiderTaskService.createListPageTasks(url, maxPage);
        return "任务创建成功";
    }

    /**
     * 查询任务列表
     */
    @GetMapping("/task/list")
    @ApiOperation(value = "查询任务列表", notes = "根据状态、任务类型查询任务列表")
    public List<CbgSpiderTask> listTasks(
            @ApiParam("任务状态：PENDING/RUNNING/SUCCESS/FAILED/STOPPED") @RequestParam(required = false) String status,
            @ApiParam("任务类型：LIST_PAGE/DETAIL_PAGE") @RequestParam(required = false) String taskType) {
        Map<String, Object> params = new HashMap<>();
        if (status != null) {
            params.put("status", status);
        }
        if (taskType != null) {
            params.put("taskType", taskType);
        }
        return cbgSpiderTaskService.listTasks(params);
    }

    /**
     * 手动启动任务
     */
    @PostMapping("/task/start/{taskId}")
    @ApiOperation(value = "手动启动任务", notes = "手动启动指定ID的任务")
    public String startTask(@ApiParam("任务ID") @PathVariable Long taskId) {
        cbgSpiderTaskExecutor.executeTaskManually(taskId);
        return "任务启动成功: " + taskId;
    }

    /**
     * 停止任务
     */
    @PostMapping("/task/stop/{taskId}")
    @ApiOperation(value = "停止任务", notes = "停止指定ID的任务")
    public String stopTask(@ApiParam("任务ID") @PathVariable Long taskId) {
        cbgSpiderTaskService.stopTask(taskId);
        return "任务已停止: " + taskId;
    }

    /**
     * 重试任务
     */
    @PostMapping("/task/retry/{taskId}")
    @ApiOperation(value = "重试任务", notes = "将失败或停止的任务重置为待执行状态")
    public String retryTask(@ApiParam("任务ID") @PathVariable Long taskId) {
        cbgSpiderTaskService.retryTask(taskId);
        return "任务已重置为待执行: " + taskId;
    }

    /**
     * 启动后台调度器
     */
    @PostMapping("/task/startScheduler")
    @ApiOperation(value = "启动后台调度器", notes = "启动后台任务调度器，自动执行待处理任务")
    public String startScheduler() {
        cbgSpiderTaskExecutor.startScheduler();
        return "后台调度器已启动";
    }

    /**
     * 停止后台调度器
     */
    @PostMapping("/task/stopScheduler")
    @ApiOperation(value = "停止后台调度器", notes = "停止后台任务调度器")
    public String stopScheduler() {
        cbgSpiderTaskExecutor.stopScheduler();
        return "后台调度器已停止";
    }

    /**
     * 查询调度器状态
     */
    @GetMapping("/task/scheduler/status")
    @ApiOperation(value = "查询调度器状态", notes = "查询后台调度器运行状态、认证状态、任务统计")
    public Map<String, Object> getSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("schedulerRunning", cbgSpiderTaskExecutor.isRunning());
        status.put("authValid", cbgAuthConfigService.checkAuthValid());
        status.put("authConfig", cbgAuthConfigService.getGlobalConfig());
        status.put("runningTaskCount", cbgSpiderTaskService.getRunningTaskCount());
        return status;
    }

    // ==================== 重新爬取接口 ====================

    /**
     * 根据物品编号重新爬取
     */
    @PostMapping("/task/replay/itemCode/{itemCode}")
    @ApiOperation(value = "根据物品编号重新爬取", notes = "根据物品编号创建重新爬取任务")
    public String replayByItemCode(@ApiParam("物品编号") @PathVariable String itemCode) {
        cbgSpiderTaskService.createReplayTaskByItemCode(itemCode);
        return "重新爬取任务已创建: " + itemCode;
    }

    /**
     * 根据物品ID重新爬取
     */
    @PostMapping("/task/replay/itemId/{itemId}")
    @ApiOperation(value = "根据物品ID重新爬取", notes = "根据物品ID创建重新爬取任务")
    public String replayByItemId(@ApiParam("物品ID") @PathVariable Long itemId) {
        cbgSpiderTaskService.createReplayTaskByItemId(itemId);
        return "重新爬取任务已创建: " + itemId;
    }

    /**
     * 批量重新爬取
     */
    @PostMapping("/task/replay/batch")
    @ApiOperation(value = "批量重新爬取", notes = "根据条件批量创建重新爬取任务")
    public String replayBatch(@ApiParam("查询条件") @RequestBody Map<String, Object> condition) {
        cbgSpiderTaskService.createReplayTasksByCondition(condition);
        return "批量重新爬取任务已创建";
    }
}
