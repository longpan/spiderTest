package com.ongl.chen.utils.spider.service.impl;

import com.ongl.chen.utils.spider.beans.cbg.CbgAuthConfig;
import com.ongl.chen.utils.spider.dao.CbgAuthConfigDAO;
import com.ongl.chen.utils.spider.dao.CbgSpiderTaskDAO;
import com.ongl.chen.utils.spider.service.CbgAuthConfigService;
import com.ongl.chen.utils.spider.utils.AppConfigFromPostForCbg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 藏宝阁全局认证配置服务实现
 */
@Service
public class CbgAuthConfigServiceImpl implements CbgAuthConfigService {

    @Autowired
    private CbgAuthConfigDAO cbgAuthConfigDAO;

    @Autowired
    private CbgSpiderTaskDAO cbgSpiderTaskDAO;

    @Override
    public void saveOrUpdateConfig(CbgAuthConfig config) {
        // 固定id=1，确保全局单例
        config.setId(1L);
        config.setUpdateTime(new Date());
        
        CbgAuthConfig existConfig = cbgAuthConfigDAO.selectById(1L);
        if (existConfig == null) {
            config.setCreateTime(new Date());
            config.setIsValid(true);
            cbgAuthConfigDAO.insert(config);
        } else {
            cbgAuthConfigDAO.updateById(config);
        }
    }

    @Override
    public CbgAuthConfig getGlobalConfig() {
        CbgAuthConfig config = cbgAuthConfigDAO.getGlobalConfig();
        if (config == null) {
            // 如果没有配置，创建一个默认的
            config = createDefaultConfig();
        }
        return config;
    }

    @Override
    public void updateAuthStatus(boolean isValid) {
        CbgAuthConfig config = getGlobalConfig();
        config.setIsValid(isValid);
        config.setLastCheckTime(new Date());
        config.setUpdateTime(new Date());
        cbgAuthConfigDAO.updateById(config);
        
        if (!isValid) {
            stopAllTasksWhenAuthInvalid();
        }
    }

    @Override
    public boolean checkAuthValid() {
        CbgAuthConfig config = getGlobalConfig();
        return config.getIsValid() != null && config.getIsValid();
    }

    @Override
    public AppConfigFromPostForCbg convertToAppConfig() {
        CbgAuthConfig config = getGlobalConfig();
        AppConfigFromPostForCbg appConfig = new AppConfigFromPostForCbg();
        
        appConfig.setSid(config.getSid());
        appConfig.setLogin_id(config.getLoginId());
        appConfig.setCbg_qrcode(config.getCbgQrcode());
        appConfig.setReco_sid(config.getRecoSid());
        appConfig.setLoginMode(config.getLoginMode());
        appConfig.setSelenuimConfig(config.getSelenuimConfig());
        appConfig.setChromeDriverPath(config.getChromeDriverPath());
        appConfig.setHeadlessMode(config.getHeadlessMode() != null ? config.getHeadlessMode() : false);
        
        return appConfig;
    }

    @Override
    public void stopAllTasksWhenAuthInvalid() {
        cbgSpiderTaskDAO.stopAllRunningTasks();
    }

    private CbgAuthConfig createDefaultConfig() {
        CbgAuthConfig config = new CbgAuthConfig();
        config.setId(1L);
        config.setSid("");
        config.setLoginId("");
        config.setCbgQrcode("");
        config.setRecoSid("");
        config.setLoginMode("auto");
        config.setSelenuimConfig("/Users/onglchen/proenv/selenium/config.ini");
        config.setChromeDriverPath("/usr/local/bin/chromedriver");
        config.setHeadlessMode(true);
        config.setIsValid(false);
        config.setCreateTime(new Date());
        config.setUpdateTime(new Date());
        cbgAuthConfigDAO.insert(config);
        return config;
    }
}
