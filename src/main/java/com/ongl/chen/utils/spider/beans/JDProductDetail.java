package com.ongl.chen.utils.spider.beans;

import com.ongl.chen.utils.spider.common.SuperEntity;

/**
 * Created by apple on 2018/8/26.
 */
public class JDProductDetail extends SuperEntity {

    String url ;
    String imgUrl ;

    String priceStr;
    String pTag;
    String pName;
    String pCommitNumStr;

    String  pShopName;
    String  pShopUrl;

    String pIcons;

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
                '}';
    }
}
