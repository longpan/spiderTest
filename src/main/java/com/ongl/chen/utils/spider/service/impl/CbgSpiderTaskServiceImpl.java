package com.ongl.chen.utils.spider.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;
import com.ongl.chen.utils.spider.beans.cbg.CbgSpiderTask;
import com.ongl.chen.utils.spider.dao.MhPetItemDAO;
import com.ongl.chen.utils.spider.dao.CbgSpiderTaskDAO;
import com.ongl.chen.utils.spider.service.CbgSpiderTaskService;
import com.ongl.chen.utils.spider.utils.UrlStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 藏宝阁爬虫任务服务实现
 */
@Service
public class CbgSpiderTaskServiceImpl implements CbgSpiderTaskService {

    @Autowired
    private CbgSpiderTaskDAO cbgSpiderTaskDAO;

    @Autowired
    private MhPetItemDAO mhPetItemDAO;

    private static final String PAGE_PARAM = "page";

    @Override
    public void createListPageTasks(String url, int maxPage) {
        // 解析URL获取基础URL和参数
        String baseUrl = UrlStringUtil.getBaseUrl(url);
        Map<String, String> params = UrlStringUtil.URLRequest(url);

        for (int page = 1; page <= maxPage; page++) {
            params.put(PAGE_PARAM, String.valueOf(page));
            String pageUrl = UrlStringUtil.constuctUrl(params, baseUrl);

            CbgSpiderTask task = new CbgSpiderTask();
            task.setTaskType("LIST_PAGE");
            task.setUrl(pageUrl);
            task.setPage(page);
            task.setStatus("PENDING");
            task.setRetryCount(0);
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());

            cbgSpiderTaskDAO.insert(task);
        }
    }

    @Override
    public void createDetailPageTask(Long parentTaskId, String detailUrl, Map<String, Object> extras) {
        CbgSpiderTask task = new CbgSpiderTask();
        task.setTaskType("DETAIL_PAGE");
        task.setParentTaskId(parentTaskId);
        task.setUrl(detailUrl);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        if (extras != null) {
            task.setExtraData(JSON.toJSONString(extras));
        }
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());

        cbgSpiderTaskDAO.insert(task);
    }

    @Override
    public List<CbgSpiderTask> getPendingTasks(int limit) {
        return cbgSpiderTaskDAO.getPendingTasks(limit);
    }

    @Override
    public void startTask(Long taskId) {
        cbgSpiderTaskDAO.startTask(taskId);
    }

    @Override
    public void completeTask(Long taskId, Long itemId, String itemCode) {
        cbgSpiderTaskDAO.completeTask(taskId, itemId, itemCode);
    }

    @Override
    public void completeTask(Long taskId, String result) {
        CbgSpiderTask task = cbgSpiderTaskDAO.selectById(taskId);
        if (task != null) {
            task.setStatus("SUCCESS");
            task.setResult(result);
            task.setEndTime(new Date());
            task.setUpdateTime(new Date());
            cbgSpiderTaskDAO.updateById(task);
        }
    }

    @Override
    public void failTask(Long taskId, String error) {
        cbgSpiderTaskDAO.failTask(taskId, error);
    }

    @Override
    public void stopTask(Long taskId) {
        cbgSpiderTaskDAO.stopTask(taskId);
    }

    @Override
    public void stopAllRunningTasks() {
        cbgSpiderTaskDAO.stopAllRunningTasks();
    }

    @Override
    public void retryTask(Long taskId) {
        CbgSpiderTask task = cbgSpiderTaskDAO.selectById(taskId);
        if (task != null) {
            task.setStatus("PENDING");
            task.setRetryCount(task.getRetryCount() + 1);
            task.setUpdateTime(new Date());
            cbgSpiderTaskDAO.updateById(task);
        }
    }

    @Override
    public int getRunningTaskCount() {
        return cbgSpiderTaskDAO.getRunningTaskCount();
    }

    @Override
    public void createReplayTaskByItemCode(String itemCode) {
        // 查询已存在的MhPetItem记录
        MhPetItem item = mhPetItemDAO.selectOne(new QueryWrapper<MhPetItem>().eq("code", itemCode));
        if (item == null) {
            throw new RuntimeException("物品编号不存在: " + itemCode);
        }

        // 获取原任务记录
        CbgSpiderTask oldTask = cbgSpiderTaskDAO.getTaskByItemCode(itemCode);
        if (oldTask == null) {
            throw new RuntimeException("未找到该物品的历史任务记录: " + itemCode);
        }

        // 创建新的详情页任务
        CbgSpiderTask newTask = new CbgSpiderTask();
        newTask.setTaskType("DETAIL_PAGE");
        newTask.setUrl(item.getDetailUrl());
        newTask.setStatus("PENDING");
        newTask.setItemCode(itemCode);
        newTask.setRetryCount(0);
        newTask.setCreateTime(new Date());
        newTask.setUpdateTime(new Date());

        cbgSpiderTaskDAO.insert(newTask);
    }

    @Override
    public void createReplayTaskByItemId(Long itemId) {
        MhPetItem item = mhPetItemDAO.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("物品ID不存在: " + itemId);
        }
        createReplayTaskByItemCode(item.getCode());
    }

    @Override
    public void createReplayTasksByCondition(Map<String, Object> condition) {
        // 根据条件查询需要重新爬取的物品
        QueryWrapper<MhPetItem> wrapper = new QueryWrapper<>();
        
        if (condition.containsKey("serverName")) {
            wrapper.eq("serverName", condition.get("serverName"));
        }
        if (condition.containsKey("area")) {
            wrapper.eq("area", condition.get("area"));
        }
        if (condition.containsKey("level")) {
            wrapper.eq("level", condition.get("level"));
        }
        if (condition.containsKey("priceMin")) {
            wrapper.ge("price", condition.get("priceMin"));
        }
        if (condition.containsKey("priceMax")) {
            wrapper.le("price", condition.get("priceMax"));
        }
        if (condition.containsKey("updateTimeBefore")) {
            wrapper.le("updateTime", condition.get("updateTimeBefore"));
        }

        List<MhPetItem> items = mhPetItemDAO.selectList(wrapper);
        for (MhPetItem item : items) {
            try {
                createReplayTaskByItemCode(item.getCode());
            } catch (Exception e) {
                // 忽略单个失败，继续处理下一个
            }
        }
    }

    @Override
    public CbgSpiderTask getTaskByItemCode(String itemCode) {
        return cbgSpiderTaskDAO.getTaskByItemCode(itemCode);
    }

    @Override
    public CbgSpiderTask getTaskById(Long taskId) {
        return cbgSpiderTaskDAO.selectById(taskId);
    }

    @Override
    public List<CbgSpiderTask> listTasks(Map<String, Object> params) {
        QueryWrapper<CbgSpiderTask> wrapper = new QueryWrapper<>();
        
        if (params != null) {
            if (params.containsKey("status")) {
                wrapper.eq("status", params.get("status"));
            }
            if (params.containsKey("taskType")) {
                wrapper.eq("taskType", params.get("taskType"));
            }
            if (params.containsKey("itemCode")) {
                wrapper.eq("itemCode", params.get("itemCode"));
            }
        }
        
        wrapper.orderByDesc("createTime");
        return cbgSpiderTaskDAO.selectList(wrapper);
    }

    @Override
    public IPage<CbgSpiderTask> listTasksPage(Map<String, Object> params) {
        int page = params.containsKey("page") ? (int) params.get("page") : 1;
        int size = params.containsKey("size") ? (int) params.get("size") : 20;
        
        QueryWrapper<CbgSpiderTask> wrapper = new QueryWrapper<>();
        
        if (params != null) {
            if (params.containsKey("status")) {
                wrapper.eq("status", params.get("status"));
            }
            if (params.containsKey("taskType")) {
                wrapper.eq("taskType", params.get("taskType"));
            }
            if (params.containsKey("itemCode")) {
                wrapper.eq("itemCode", params.get("itemCode"));
            }
            
            // 排序
            String sortField = params.containsKey("sortField") ? (String) params.get("sortField") : "createTime";
            String sortOrder = params.containsKey("sortOrder") ? (String) params.get("sortOrder") : "desc";
            
            if ("asc".equalsIgnoreCase(sortOrder)) {
                wrapper.orderByAsc(sortField);
            } else {
                wrapper.orderByDesc(sortField);
            }
        }
        
        return cbgSpiderTaskDAO.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public int batchDeleteTasks(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return 0;
        }
        return cbgSpiderTaskDAO.deleteBatchIds(taskIds);
    }

    @Override
    public int batchRetryTasks(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Long taskId : taskIds) {
            CbgSpiderTask task = cbgSpiderTaskDAO.selectById(taskId);
            if (task != null && ("FAILED".equals(task.getStatus()) || "STOPPED".equals(task.getStatus()))) {
                task.setStatus("PENDING");
                task.setRetryCount(task.getRetryCount() + 1);
                task.setResult(null);
                task.setUpdateTime(new Date());
                cbgSpiderTaskDAO.updateById(task);
                count++;
            }
        }
        return count;
    }

    @Override
    public void reRunTask(Long taskId) {
        CbgSpiderTask task = cbgSpiderTaskDAO.selectById(taskId);
        if (task != null) {
            task.setStatus("PENDING");
            task.setRetryCount(0);
            task.setResult(null);
            task.setEndTime(null);
            task.setUpdateTime(new Date());
            cbgSpiderTaskDAO.updateById(task);
        }
    }

    @Override
    public int batchReRunTasks(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Long taskId : taskIds) {
            CbgSpiderTask task = cbgSpiderTaskDAO.selectById(taskId);
            if (task != null && !"RUNNING".equals(task.getStatus())) {
                task.setStatus("PENDING");
                task.setRetryCount(0);
                task.setResult(null);
                task.setEndTime(null);
                task.setUpdateTime(new Date());
                cbgSpiderTaskDAO.updateById(task);
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean reRunTaskByUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        // 根据URL查找任务
        CbgSpiderTask task = cbgSpiderTaskDAO.getTaskByUrl(url);
        if (task == null) {
            // 如果没有找到任务，创建一个新的详情页任务
            CbgSpiderTask newTask = new CbgSpiderTask();
            newTask.setTaskType("DETAIL_PAGE");
            newTask.setUrl(url);
            newTask.setStatus("PENDING");
            newTask.setRetryCount(0);
            newTask.setCreateTime(new Date());
            newTask.setUpdateTime(new Date());
            cbgSpiderTaskDAO.insert(newTask);
            return true;
        }
        
        // 如果任务正在运行中，不能重新运行
        if ("RUNNING".equals(task.getStatus())) {
            return false;
        }
        
        // 重置任务状态为PENDING
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setResult(null);
        task.setEndTime(null);
        task.setUpdateTime(new Date());
        cbgSpiderTaskDAO.updateById(task);
        return true;
    }
}
