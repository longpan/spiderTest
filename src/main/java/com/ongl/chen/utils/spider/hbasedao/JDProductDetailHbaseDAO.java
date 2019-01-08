package com.ongl.chen.utils.spider.hbasedao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import com.ongl.chen.utils.spider.beans.JDProductDetail;


/**
 * @author code4crafer@gmail.com
 *         Date: 13-6-23
 *         Time: 下午4:27
 */
public interface JDProductDetailHbaseDAO  {

	@Insert("upsert into jd_product_detail (ID,URL,IMG_URL,PRICE,PRICE_STR,P_TAG,P_NAME,P_COMMIT_NUM,P_COMMIT_NUM_STR,P_SHOP_NAME,P_SHOP_URL,P_ICONS,P_TYPE,P_ID,P_PAGE)" +
			" VALUES (#{detail.id},#{detail.url},#{detail.imgUrl},#{detail.price},#{detail.priceStr},#{detail.pTag},#{detail.pName},#{detail.commitNum},#{detail.pCommitNumStr},#{detail.pShopName},#{detail.pShopUrl},#{detail.pIcons},#{detail.type},#{detail.PId},#{detail.pPage})")
	  public void addProduct(@Param("detail") JDProductDetail jdProductDetail);

}
