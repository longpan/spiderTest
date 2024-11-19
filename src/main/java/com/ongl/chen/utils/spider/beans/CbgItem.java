package com.ongl.chen.utils.spider.beans;

import com.alibaba.excel.annotation.ExcelProperty;
import com.ongl.chen.utils.spider.common.SuperEntity;

public class CbgItem extends SuperEntity {

    @ExcelProperty("门派")
    String wrapName ;

    @ExcelProperty("等级")
    String level ;

    @ExcelProperty("服务器")
    String serverName ;

    @ExcelProperty("总评分")
    String overallScore ;

    @ExcelProperty("人物评分")
    String personScore ;

    @ExcelProperty("价格")
    String price ;

    @ExcelProperty("性价比")
    String txt ;

    @ExcelProperty("收藏人数")
    String collect ;


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
}
