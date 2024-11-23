package com.ongl.chen.utils.spider.beans;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ongl.chen.utils.spider.common.SuperEntity;

import java.util.Date;

@TableName(value = "cbg_item")
public class CbgItem extends SuperEntity {

    @TableField(value = "code")
    @ExcelProperty("编号")
    String code ;
    @TableField(value = "wrapName")
    @ExcelProperty("门派")
    String wrapName ;
    @TableField(value = "level")
    @ExcelProperty("等级")
    String level ;
    @TableField(value = "publicity")
    @ExcelProperty("公标志")
    String publicity;
    @TableField(value = "bargin")
    @ExcelProperty("还标志")
    String bargin;

    @TableField(value = "draw")
    @ExcelProperty("抽标志")
    String draw;
    @TableField(value = "serverName")
    @ExcelProperty("服务器")
    String serverName ;

    @TableField(value = "serverId")
    @ExcelProperty("服务器id")
    String serverId;
    @TableField(value = "overallScore")
    @ExcelProperty("总评分")
    String overallScore ;
    @TableField(value = "personScore")
    @ExcelProperty("人物评分")
    String personScore ;
    @TableField(value = "price")
    @ExcelProperty("价格")
    String price ;
    @TableField(value = "txt")
    @ExcelProperty("性价比")
    String txt ;
    @TableField(value = "collect")
    @ExcelProperty("收藏人数")
    String collect ;
    @TableField(value = "detailUrl")
    @ExcelProperty("链接")
    String detailUrl;



    public String getWrapName() {
        return wrapName;
    }

    public void setWrapName(String wrapName) {
        this.wrapName = wrapName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(String overallScore) {
        this.overallScore = overallScore;
    }

    public String getPersonScore() {
        return personScore;
    }

    public void setPersonScore(String personScore) {
        this.personScore = personScore;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getCollect() {
        return collect;
    }

    public void setCollect(String collect) {
        this.collect = collect;
    }

    public String getPublicity() {
        return publicity;
    }

    public void setPublicity(String publicity) {
        this.publicity = publicity;
    }

    public String getBargin() {
        return bargin;
    }

    public void setBargin(String bargin) {
        this.bargin = bargin;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getDraw() {
        return draw;
    }

    public void setDraw(String draw) {
        this.draw = draw;
    }
}
