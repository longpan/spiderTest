package com.ongl.chen.utils.spider.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ongl.chen.utils.spider.beans.cbg.CbgSpiderTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 藏宝阁爬虫任务DAO
 */
public interface CbgSpiderTaskDAO extends BaseMapper<CbgSpiderTask> {

    /**
     * 获取待执行任务
     */
    @Select("SELECT * FROM cbg_spider_task WHERE status = 'PENDING' ORDER BY createTime ASC LIMIT #{limit}")
    List<CbgSpiderTask> getPendingTasks(@Param("limit") int limit);

    /**
     * 获取运行中任务数量
     */
    @Select("SELECT COUNT(*) FROM cbg_spider_task WHERE status = 'RUNNING'")
    int getRunningTaskCount();

    /**
     * 停止所有运行中任务
     */
    @Update("UPDATE cbg_spider_task SET status = 'FAILED', result = '认证失效自动停止', endTime = NOW() WHERE status = 'RUNNING'")
    int stopAllRunningTasks();

    /**
     * 根据物品编号查询任务
     */
    @Select("SELECT * FROM cbg_spider_task WHERE itemCode = #{itemCode} ORDER BY createTime DESC LIMIT 1")
    CbgSpiderTask getTaskByItemCode(@Param("itemCode") String itemCode);

    /**
     * 更新任务状态为运行中
     */
    @Update("UPDATE cbg_spider_task SET status = 'RUNNING', startTime = NOW(), updateTime = NOW() WHERE id = #{taskId}")
    int startTask(@Param("taskId") Long taskId);

    /**
     * 完成任务
     */
    @Update("UPDATE cbg_spider_task SET status = 'SUCCESS', itemId = #{itemId}, itemCode = #{itemCode}, " +
            "endTime = NOW(), updateTime = NOW() WHERE id = #{taskId}")
    int completeTask(@Param("taskId") Long taskId, @Param("itemId") Long itemId, @Param("itemCode") String itemCode);

    /**
     * 任务失败
     */
    @Update("UPDATE cbg_spider_task SET status = 'FAILED', result = #{result}, endTime = NOW(), updateTime = NOW() WHERE id = #{taskId}")
    int failTask(@Param("taskId") Long taskId, @Param("result") String result);

    /**
     * 停止任务
     */
    @Update("UPDATE cbg_spider_task SET status = 'STOPPED', endTime = NOW(), updateTime = NOW() WHERE id = #{taskId}")
    int stopTask(@Param("taskId") Long taskId);
}
