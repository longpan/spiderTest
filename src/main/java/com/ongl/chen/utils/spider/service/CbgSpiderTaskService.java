package com.ongl.chen.utils.spider.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ongl.chen.utils.spider.beans.cbg.CbgSpiderTask;

import java.util.List;
import java.util.Map;

/**
 * 藏宝阁爬虫任务服务
 */
public interface CbgSpiderTaskService {

    /**
     * 创建列表页任务
     */
    void createListPageTasks(String url, int maxPage);

    /**
     * 创建详情页子任务
     */
    void createDetailPageTask(Long parentTaskId, String detailUrl, Map<String, Object> extras);

    /**
     * 获取待执行任务
     */
    List<CbgSpiderTask> getPendingTasks(int limit);

    /**
     * 开始执行任务
     */
    void startTask(Long taskId);

    /**
     * 完成任务并关联数据
     */
    void completeTask(Long taskId, Long itemId, String itemCode);

    /**
     * 完成任务（不关联数据）
     */
    void completeTask(Long taskId, String result);

    /**
     * 任务失败
     */
    void failTask(Long taskId, String error);

    /**
     * 停止任务
     */
    void stopTask(Long taskId);

    /**
     * 停止所有运行中任务（认证失效时调用）
     */
    void stopAllRunningTasks();

    /**
     * 重试任务
     */
    void retryTask(Long taskId);

    /**
     * 获取运行中任务数
     */
    int getRunningTaskCount();

    /**
     * 根据物品编号重新创建爬取任务
     */
    void createReplayTaskByItemCode(String itemCode);

    /**
     * 根据物品ID重新创建爬取任务
     */
    void createReplayTaskByItemId(Long itemId);

    /**
     * 批量重新爬取
     */
    void createReplayTasksByCondition(Map<String, Object> condition);

    /**
     * 获取物品关联的任务记录
     */
    CbgSpiderTask getTaskByItemCode(String itemCode);

    /**
     * 根据ID获取任务
     */
    CbgSpiderTask getTaskById(Long taskId);

    /**
     * 查询任务列表
     */
    List<CbgSpiderTask> listTasks(Map<String, Object> params);

    /**
     * 分页查询任务列表
     */
    IPage<CbgSpiderTask> listTasksPage(Map<String, Object> params);
}
