package com.ongl.chen.utils.spider.beans.cbg;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.ongl.chen.utils.spider.common.SuperEntity;

import java.util.Date;

/**
 * 藏宝阁全局认证配置表
 * 全局单例模式，固定id=1，所有爬虫任务共用一份认证信息
 */
@TableName(value = "cbg_auth_config")
public class CbgAuthConfig extends SuperEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 登录会话ID
     */
    @TableField(value = "sid")
    @Column(name = "sid", comment = "登录会话ID", length = 512)
    private String sid;

    /**
     * 登录账号
     */
    @TableField(value = "loginId")
    @Column(name = "loginId", comment = "登录账号", length = 128)
    private String loginId;

    /**
     * 藏宝阁二维码凭证
     */
    @TableField(value = "cbgQrcode")
    @Column(name = "cbgQrcode", comment = "藏宝阁二维码凭证", length = 512)
    private String cbgQrcode;

    /**
     * 推荐会话ID
     */
    @TableField(value = "recoSid")
    @Column(name = "recoSid", comment = "推荐会话ID", length = 512)
    private String recoSid;

    /**
     * 登录模式：auto/cookie
     */
    @TableField(value = "loginMode")
    @Column(name = "loginMode", comment = "登录模式：auto/cookie", length = 32)
    private String loginMode;

    /**
     * Selenium配置文件路径
     */
    @TableField(value = "selenuimConfig")
    @Column(name = "selenuimConfig", comment = "Selenium配置文件路径", length = 512)
    private String selenuimConfig;

    /**
     * ChromeDriver路径
     */
    @TableField(value = "chromeDriverPath")
    @Column(name = "chromeDriverPath", comment = "ChromeDriver路径", length = 512)
    private String chromeDriverPath;

    /**
     * 是否无头模式
     */
    @TableField(value = "headlessMode")
    @Column(name = "headlessMode", comment = "是否无头模式")
    private Boolean headlessMode;

    /**
     * 认证是否有效
     */
    @TableField(value = "isValid")
    @Column(name = "isValid", comment = "认证是否有效")
    private Boolean isValid;

    /**
     * 上次认证检查时间
     */
    @TableField(value = "lastCheckTime")
    @Column(name = "lastCheckTime", comment = "上次认证检查时间")
    private Date lastCheckTime;

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

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getCbgQrcode() {
        return cbgQrcode;
    }

    public void setCbgQrcode(String cbgQrcode) {
        this.cbgQrcode = cbgQrcode;
    }

    public String getRecoSid() {
        return recoSid;
    }

    public void setRecoSid(String recoSid) {
        this.recoSid = recoSid;
    }

    public String getLoginMode() {
        return loginMode;
    }

    public void setLoginMode(String loginMode) {
        this.loginMode = loginMode;
    }

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

    public Boolean getHeadlessMode() {
        return headlessMode;
    }

    public void setHeadlessMode(Boolean headlessMode) {
        this.headlessMode = headlessMode;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public Date getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
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
}
