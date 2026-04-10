package com.ongl.chen.utils.spider.service;

import com.ongl.chen.utils.spider.beans.cbg.CbgAuthConfig;
import com.ongl.chen.utils.spider.utils.AppConfigFromPostForCbg;

/**
 * 藏宝阁全局认证配置服务
 */
public interface CbgAuthConfigService {

    /**
     * 保存或更新全局认证配置（固定id=1）
     */
    void saveOrUpdateConfig(CbgAuthConfig config);

    /**
     * 获取全局认证配置
     */
    CbgAuthConfig getGlobalConfig();

    /**
     * 更新认证状态（失效时调用）
     */
    void updateAuthStatus(boolean isValid);

    /**
     * 检查认证是否有效
     */
    boolean checkAuthValid();

    /**
     * 转换为AppConfigFromPostForCbg
     */
    AppConfigFromPostForCbg convertToAppConfig();

    /**
     * 认证失效时停止所有任务
     */
    void stopAllTasksWhenAuthInvalid();
}
