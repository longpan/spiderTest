package com.ongl.chen.utils.spider.utils;

public class AppConfigFromPostForCbg {


    private String selenuimConfig;

    private String chromeDriverPath;


    private String detailUrl;

    private int maxPage;

    private String sid;

    private String login_id;

    private String cbg_qrcode;

    private String reco_sid;



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

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getLogin_id() {
        return login_id;
    }

    public void setLogin_id(String login_id) {
        this.login_id = login_id;
    }

    public String getCbg_qrcode() {
        return cbg_qrcode;
    }

    public void setCbg_qrcode(String cbg_qrcode) {
        this.cbg_qrcode = cbg_qrcode;
    }

    public String getReco_sid() {
        return reco_sid;
    }

    public void setReco_sid(String reco_sid) {
        this.reco_sid = reco_sid;
    }
}