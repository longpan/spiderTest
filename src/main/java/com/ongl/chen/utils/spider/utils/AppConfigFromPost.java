package com.ongl.chen.utils.spider.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public class AppConfigFromPost {


    private String selenuimConfig;

    private String chromeDriverPath;

    private String resultSavePath;

    //最大下拉刷新次数
    private int maxPullDownCount = 1000;

    //每次获取最大列表个数
    private int maxItemCount = 2000;

    //下拉刷新时睡眠时间（毫秒）
    private int pullDownSleepTimeMillis = 3000;

    //获取详细链接睡眠时间（毫秒）
    private int getDetailUrlSleepTimeMillis = 3000;


    private boolean headlessMode;


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

    public boolean isHeadlessMode() {
        return headlessMode;
    }

    public void setHeadlessMode(boolean headlessMode) {
        this.headlessMode = headlessMode;
    }

    public String getResultSavePath() {
        return resultSavePath;
    }

    public void setResultSavePath(String resultSavePath) {
        this.resultSavePath = resultSavePath;
    }
}