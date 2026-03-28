package com.ongl.chen.utils.spider.beans.dbg;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Table;
import com.ongl.chen.utils.spider.common.SuperEntity;

import java.util.Date;

@TableName(value = "mh_equip_item")
//@Table(name = "mh_equip_item")
public class MhEquipItem extends SuperEntity {

    @TableField(value = "code")
    @Column(name = "code",comment = "编号")
    @ExcelProperty("编号")
    String code ;

    @TableField(value = "name")
    @Column(name = "name",comment = "名称")
    String name;

    @TableField(value = "level")
    @Column(name = "level",comment = "等级")
    @ExcelProperty("等级")
    String level ;

    @TableField(value = "serverName")
    @Column(name = "serverName",comment = "服务器")
    @ExcelProperty("服务器")
    String serverName ;

    @TableField(value = "serverId")
    @Column(name = "serverId",comment = "服务器id")
    @ExcelProperty("服务器id")
    String serverId;

    @TableField(value = "price")
    @Column(name = "price",comment = "价格")
    @ExcelProperty("价格")
    String price ;

    @TableField(value = "collect")
    @Column(name = "collect",comment = "收藏人数")
    @ExcelProperty("收藏人数")
    String collect ;



    @TableField(value = "lightSpot1")
    @Column(name = "lightSpot1",comment = "亮点1", length = 2048)
    String lightSpot1;

    @TableField(value = "lightSpot2")
    @Column(name = "lightSpot2",comment = "亮点2", length = 2048)
    String lightSpot2;

    @TableField(value = "primeAttribute")
    @Column(name = "primeAttribute",comment = "主属性")
    String primeAttribute;


    @TableField(value = "accessoryAttributeNum")
    @Column(name = "accessoryAttributeNum",comment = "附属性数量")
    int accessoryAttributeNum;


    @TableField(value = "accessoryAttribute")
    @Column(name = "accessoryAttribute",comment = "附属性")
    String accessoryAttribute;


    @TableField(value = "specialEffects")
    @Column(name = "specialEffects",comment = "特效")
    String specialEffects;

    @TableField(value = "runestones")
    @Column(name = "runestones",comment = "符石")
    String runestones;

    @TableField(value = "descYellow")
    @Column(name = "descYellow",comment = "黄字描述")
    String descYellow;

    @TableField(value = "createTime")
    @Column(name = "createTime",comment = "创建时间") // name指定数据库字段名，comment为备注
    private Date createTime;
    /**
     * 最后修改时间
     */
    @TableField(value = "updateTime")
    @Column(name = "updateTime",comment = "最后修改时间")
    private Date updateTime;


    @TableField(value = "detailUrl")
    @Column(name = "detailUrl",comment = "链接",length = 2048)
    @ExcelProperty("链接")
    String detailUrl;

    @TableField(value = "valuationValue")
    @Column(name = "valuationValue",comment = "估值")
    @ExcelProperty("估值")
    private Double valuationValue;

    @TableField(value = "valuationScore")
    @Column(name = "valuationScore",comment = "性价比")
    @ExcelProperty("性价比")
    private Double valuationScore;

    @TableField(value = "txt")
    @Column(name = "txt",comment = "评价", length = 2048)
    @ExcelProperty("评价")
    private String txt;

    public Double getValuationValue() {
        return valuationValue;
    }

    public void setValuationValue(Double valuationValue) {
        this.valuationValue = valuationValue;
    }

    public Double getValuationScore() {
        return valuationScore;
    }

    public void setValuationScore(Double valuationScore) {
        this.valuationScore = valuationScore;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCollect() {
        return collect;
    }

    public void setCollect(String collect) {
        this.collect = collect;
    }

    public String getLightSpot1() {
        return lightSpot1;
    }

    public void setLightSpot1(String lightSpot1) {
        this.lightSpot1 = lightSpot1;
    }

    public String getLightSpot2() {
        return lightSpot2;
    }

    public void setLightSpot2(String lightSpot2) {
        this.lightSpot2 = lightSpot2;
    }

    public String getDescYellow() {
        return descYellow;
    }

    public void setDescYellow(String descYellow) {
        this.descYellow = descYellow;
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

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }


    public String getPrimeAttribute() {
        return primeAttribute;
    }

    public void setPrimeAttribute(String primeAttribute) {
        this.primeAttribute = primeAttribute;
    }

    public int getAccessoryAttributeNum() {
        return accessoryAttributeNum;
    }

    public void setAccessoryAttributeNum(int accessoryAttributeNum) {
        this.accessoryAttributeNum = accessoryAttributeNum;
    }

    public String getAccessoryAttribute() {
        return accessoryAttribute;
    }

    public void setAccessoryAttribute(String accessoryAttribute) {
        this.accessoryAttribute = accessoryAttribute;
    }

    public String getRunestones() {
        return runestones;
    }

    public void setRunestones(String runestones) {
        this.runestones = runestones;
    }

    public String getSpecialEffects() {
        return specialEffects;
    }

    public void setSpecialEffects(String specialEffects) {
        this.specialEffects = specialEffects;
    }
}
