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

	@Insert("upsert into jd_product_detail (ID,P_NAME) VALUES (#{jdProductDetail.id},#{jdProductDetail.pName})")
	  public void addProduct(@Param("jdProductDetail") JDProductDetail jdProductDetail);

}
