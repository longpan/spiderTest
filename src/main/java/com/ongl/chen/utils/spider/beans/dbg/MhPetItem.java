package com.ongl.chen.utils.spider.beans.dbg;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.ongl.chen.utils.spider.common.SuperEntity;

import com.gitee.sunchenbin.mybatis.actable.annotation.Table;

import java.util.Date;

@TableName(value = "mh_pet_item")
@Table(name = "mh_pet_item")
public class MhPetItem extends SuperEntity {

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
    @Column(name = "lightSpot1",comment = "亮点1", length = 4096)
    String lightSpot1;

    @TableField(value = "lightSpot2")
    @Column(name = "lightSpot2",comment = "亮点2", length = 4096)
    String lightSpot2;

    @TableField(value = "skillNum")
    @Column(name = "skillNum",comment = "技能数量")
    int skillNum;

    @TableField(value = "skillList")
    @Column(name = "skillList",comment = "技能列表", length = 4096)
    String skillList;

    //气血
    @TableField(value = "blood")
    @Column(name = "blood",comment = "气血")
    String blood;

    //体质
    @TableField(value = "corporeity")
    @Column(name = "corporeity",comment = "体质")
    String corporeity;

    //魔法
    @TableField(value = "magic")
    @Column(name = "magic",comment = "魔法")
    String magic;

    //法力
    @TableField(value = "power")
    @Column(name = "power",comment = "法力")
    String power;

    //攻击
    @TableField(value = "attack")
    @Column(name = "attack",comment = "攻击")
    String attack;

    //力量
    @TableField(value = "strength")
    @Column(name = "strength",comment = "力量")
    String strength;

    //防御
    @TableField(value = "defense")
    @Column(name = "defense",comment = "防御")
    String defense;

    //耐力
    @TableField(value = "endurance")
    @Column(name = "endurance",comment = "耐力")
    String endurance;

    //速度
    @TableField(value = "speed")
    @Column(name = "speed",comment = "速度")
    String speed;

    //敏捷
    @TableField(value = "agility")
    @Column(name = "agility",comment = "敏捷")
    String agility;

    //法伤
    @TableField(value = "mageWound")
    @Column(name = "mageWound",comment = "法伤")
    String mageWound;

    //潜能
    @TableField(value = "potency")
    @Column(name = "potency",comment = "潜能")
    String potency;

    //法防
    @TableField(value = "mageDefence")
    @Column(name = "mageDefence",comment = "法防")
    String mageDefence;

    //攻击资质
    @TableField(value = "attackQualification")
    @Column(name = "attackQualification",comment = "攻击资质")
    String attackQualification;

    //寿命
    @TableField(value = "lifetime")
    @Column(name = "lifetime",comment = "寿命")
    String lifetime;

    //防御资质
    @TableField(value = "defenseQualification")
    @Column(name = "defenseQualification",comment = "防御资质")
    String defenseQualification;

    //成长
    @TableField(value = "growUp")
    @Column(name = "growUp",comment = "成长")
    String growUp;

    //体力资质
    @TableField(value = "physicalQualification")
    @Column(name = "physicalQualification",comment = "体力资质")
    String physicalQualification;

    //法力资质
    @TableField(value = "manaQualification")
    @Column(name = "manaQualification",comment = "法力资质")
    String manaQualification;

    //速度资质
    @TableField(value = "speedQualification")
    @Column(name = "speedQualification",comment = "速度资质")
    String speedQualification;

    //dodgeQualification
    @TableField(value = "dodgeQualification")
    @Column(name = "dodgeQualification",comment = "dodgeQualification")
    String dodgeQualification;

    //是否宝宝
    @TableField(value = "isBaby")
    @Column(name = "isBaby",comment = "是否宝宝")
    String isBaby;

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
    @Column(name = "detailUrl",comment = "链接",length = 4096)
    @ExcelProperty("链接")
    String detailUrl;

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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getSkillNum() {
        return skillNum;
    }

    public void setSkillNum(int skillNum) {
        this.skillNum = skillNum;
    }

    public String getSkillList() {
        return skillList;
    }

    public void setSkillList(String skillList) {
        this.skillList = skillList;
    }

    public String getBlood() {
        return blood;
    }

    public void setBlood(String blood) {
        this.blood = blood;
    }

    public String getCorporeity() {
        return corporeity;
    }

    public void setCorporeity(String corporeity) {
        this.corporeity = corporeity;
    }

    public String getMagic() {
        return magic;
    }

    public void setMagic(String magic) {
        this.magic = magic;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getAttack() {
        return attack;
    }

    public void setAttack(String attack) {
        this.attack = attack;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getDefense() {
        return defense;
    }

    public void setDefense(String defense) {
        this.defense = defense;
    }

    public String getEndurance() {
        return endurance;
    }

    public void setEndurance(String endurance) {
        this.endurance = endurance;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getAgility() {
        return agility;
    }

    public void setAgility(String agility) {
        this.agility = agility;
    }

    public String getMageWound() {
        return mageWound;
    }

    public void setMageWound(String mageWound) {
        this.mageWound = mageWound;
    }

    public String getPotency() {
        return potency;
    }

    public void setPotency(String potency) {
        this.potency = potency;
    }

    public String getMageDefence() {
        return mageDefence;
    }

    public void setMageDefence(String mageDefence) {
        this.mageDefence = mageDefence;
    }

    public String getAttackQualification() {
        return attackQualification;
    }

    public void setAttackQualification(String attackQualification) {
        this.attackQualification = attackQualification;
    }

    public String getLifetime() {
        return lifetime;
    }

    public void setLifetime(String lifetime) {
        this.lifetime = lifetime;
    }

    public String getDefenseQualification() {
        return defenseQualification;
    }

    public void setDefenseQualification(String defenseQualification) {
        this.defenseQualification = defenseQualification;
    }

    public String getGrowUp() {
        return growUp;
    }

    public void setGrowUp(String growUp) {
        this.growUp = growUp;
    }

    public String getPhysicalQualification() {
        return physicalQualification;
    }

    public void setPhysicalQualification(String physicalQualification) {
        this.physicalQualification = physicalQualification;
    }

    public String getManaQualification() {
        return manaQualification;
    }

    public void setManaQualification(String manaQualification) {
        this.manaQualification = manaQualification;
    }

    public String getSpeedQualification() {
        return speedQualification;
    }

    public void setSpeedQualification(String speedQualification) {
        this.speedQualification = speedQualification;
    }

    public String getDodgeQualification() {
        return dodgeQualification;
    }

    public void setDodgeQualification(String dodgeQualification) {
        this.dodgeQualification = dodgeQualification;
    }

    public String getIsBaby() {
        return isBaby;
    }

    public void setIsBaby(String isBaby) {
        this.isBaby = isBaby;
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

    public String getCollect() {
        return collect;
    }

    public void setCollect(String collect) {
        this.collect = collect;
    }
}
