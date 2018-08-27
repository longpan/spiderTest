package com.ongl.chen.utils.spider.beans;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.ongl.chen.utils.spider.common.SuperEntity;

/**
 * Created by apple on 2018/8/26.
 */
@TableName("jd_product_detail")
public class JDProductDetail extends SuperEntity {

    String url ;

    @TableField("img_url")
    String imgUrl ;

    @TableField("price_str")
    String priceStr;

    @TableField("p_tag")
    String pTag;

    @TableField("p_name")
    String pName;

    @TableField("p_commit_num")
    String pCommitNumStr;

    @TableField("p_shop_name")
    String  pShopName;

    @TableField("p_shop_url")
    String  pShopUrl;

    @TableField("p_icons")
    String pIcons;

    @TableField("p_type")
    String type;

    @TableField("p_id")
    String PId;



    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPriceStr() {
        return priceStr;
    }

    public void setPriceStr(String priceStr) {
        this.priceStr = priceStr;
    }

    public String getpTag() {
        return pTag;
    }

    public void setpTag(String pTag) {
        this.pTag = pTag;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public String getpCommitNumStr() {
        return pCommitNumStr;
    }

    public void setpCommitNumStr(String pCommitNumStr) {
        this.pCommitNumStr = pCommitNumStr;
    }

    public String getpShopName() {
        return pShopName;
    }

    public void setpShopName(String pShopName) {
        this.pShopName = pShopName;
    }

    public String getpShopUrl() {
        return pShopUrl;
    }

    public void setpShopUrl(String pShopUrl) {
        this.pShopUrl = pShopUrl;
    }

    public String getpIcons() {
        return pIcons;
    }

    public void setpIcons(String pIcons) {
        this.pIcons = pIcons;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPId() {
        return PId;
    }

    public void setPId(String PId) {
        this.PId = PId;
    }

    @Override
    public String toString() {
        return "JDProductDetail{" +
                "url='" + url + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", priceStr='" + priceStr + '\'' +
                ", pTag='" + pTag + '\'' +
                ", pName='" + pName + '\'' +
                ", pCommitNumStr='" + pCommitNumStr + '\'' +
                ", pShopName='" + pShopName + '\'' +
                ", pShopUrl='" + pShopUrl + '\'' +
                ", pIcons='" + pIcons + '\'' +
                ", type='" + type + '\'' +
                ", PId='" + PId + '\'' +
                '}';
    }
}
