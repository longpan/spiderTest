package com.ongl.chen.utils.spider.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {


    @Value("${selenuim.config}")
    private String selenuimConfig;

    @Value("${chrome.driver.path}")
    private String chromeDriverPath;

    //最大下拉刷新次数
    @Value("${max.pull-down-count}")
    private int maxPullDownCount = 1000;

    //每次获取最大列表个数
    @Value("${max.item-count}")
    private int maxItemCount = 2000;

    //下拉刷新时睡眠时间（毫秒）
    @Value("${pull-down.sleep-time-millis}")
    private int pullDownSleepTimeMillis = 3000;

    //获取详细链接睡眠时间（毫秒）
    @Value("${get-detail-url.sleep-time-millis}")
    private int getDetailUrlSleepTimeMillis = 3000;


    public String getSelenuimConfig() {
        return selenuimConfig;
    }

    public void setSelenuimConfig(String selenuimConfig) {
        this.selenuimConfig = selenuimConfig;
    }

    public String getChromeDriverPath() {
        return chromeDriverPath;
    }

    public void setChromeDriverPath(String chromeDriverPath) {
        this.chromeDriverPath = chromeDriverPath;
    }

    public int getMaxPullDownCount() {
        return maxPullDownCount;
    }

    public void setMaxPullDownCount(int maxPullDownCount) {
        this.maxPullDownCount = maxPullDownCount;
    }

    public int getMaxItemCount() {
        return maxItemCount;
    }

    public void setMaxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
    }

    public int getPullDownSleepTimeMillis() {
        return pullDownSleepTimeMillis;
    }

    public void setPullDownSleepTimeMillis(int pullDownSleepTimeMillis) {
        this.pullDownSleepTimeMillis = pullDownSleepTimeMillis;
    }

    public int getGetDetailUrlSleepTimeMillis() {
        return getDetailUrlSleepTimeMillis;
    }

    public void setGetDetailUrlSleepTimeMillis(int getDetailUrlSleepTimeMillis) {
        this.getDetailUrlSleepTimeMillis = getDetailUrlSleepTimeMillis;
    }
}