package com.ongl.chen.utils.spider.beans.cbg;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.ongl.chen.utils.spider.common.SuperEntity;

import java.util.Date;

/**
 * 藏宝阁爬虫任务表
 * 统一任务表，合并原任务表和详情表
 */
@TableName(value = "cbg_spider_task")
public class CbgSpiderTask extends SuperEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 任务类型：LIST_PAGE(列表页)/DETAIL_PAGE(详情页)
     */
    @TableField(value = "taskType")
    @Column(name = "taskType", comment = "任务类型：LIST_PAGE/DETAIL_PAGE", length = 32)
    private String taskType;

    /**
     * 父任务ID，详情页关联列表页
     */
    @TableField(value = "parentTaskId")
    @Column(name = "parentTaskId", comment = "父任务ID，详情页关联列表页")
    private Long parentTaskId;

    /**
     * 爬取URL
     */
    @TableField(value = "url")
    @Column(name = "url", comment = "爬取URL", length = 2048)
    private String url;

    /**
     * 页码，列表页专用
     */
    @TableField(value = "page")
    @Column(name = "page", comment = "页码，列表页专用")
    private Integer page;

    /**
     * 任务状态：PENDING(待执行)/RUNNING(执行中)/SUCCESS(成功)/FAILED(失败)/STOPPED(已停止)
     */
    @TableField(value = "status")
    @Column(name = "status", comment = "任务状态：PENDING/RUNNING/SUCCESS/FAILED/STOPPED", length = 32)
    private String status;

    /**
     * 执行结果/错误信息
     */
    @TableField(value = "result")
    @Column(name = "result", comment = "执行结果/错误信息", length = 2048)
    private String result;

    /**
     * 重试次数
     */
    @TableField(value = "retryCount")
    @Column(name = "retryCount", comment = "重试次数")
    private Integer retryCount;

    /**
     * 解析出的物品编号，关联CbgItem.code
     */
    @TableField(value = "itemCode")
    @Column(name = "itemCode", comment = "解析出的物品编号，关联CbgItem.code", length = 128)
    private String itemCode;

    /**
     * 关联的CbgItem主键ID
     */
    @TableField(value = "itemId")
    @Column(name = "itemId", comment = "关联的CbgItem主键ID")
    private Long itemId;

    /**
     * 额外数据JSON：price/collect/lightSpot等
     */
    @TableField(value = "extraData")
    @Column(name = "extraData", comment = "额外数据JSON：price/collect/lightSpot等", length = 2048)
    private String extraData;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    @Column(name = "createTime", comment = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    @Column(name = "updateTime", comment = "更新时间")
    private Date updateTime;

    /**
     * 开始执行时间
     */
    @TableField(value = "startTime")
    @Column(name = "startTime", comment = "开始执行时间")
    private Date startTime;

    /**
     * 结束执行时间
     */
    @TableField(value = "endTime")
    @Column(name = "endTime", comment = "结束执行时间")
    private Date endTime;

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Long getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(Long parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
