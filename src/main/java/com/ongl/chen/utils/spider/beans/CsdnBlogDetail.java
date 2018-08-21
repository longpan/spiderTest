
/**
* @Title: CsdnBlogDetail.java
* @Package com.ongl.chen.utils.spider.beans
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.beans;

import com.baomidou.mybatisplus.annotations.TableName;
import com.ongl.chen.utils.spider.common.SuperEntity;

/**
* @ClassName: CsdnBlogDetail
* @Description: TODO(这里用一句话描述这个类的作用)
* @author maojing.long
* @date 2018年8月15日
*
*/
@TableName("csdn_blog")
public class CsdnBlogDetail extends SuperEntity
{
	
	String title;
	
	String pref;
	
	String url;
	
	String context;
	
	String blogId;

	String readCount;

	String creatTime;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPref() {
		return pref;
	}

	public void setPref(String pref) {
		this.pref = pref;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}


	public String getBlogId() {
		return blogId;
	}

	public void setBlogId(String blogId) {
		this.blogId = blogId;
	}

	public String getReadCount() {
		return readCount;
	}

	public void setReadCount(String readCount) {
		this.readCount = readCount;
	}

	public String getCreatTime() {
		return creatTime;
	}

	public void setCreatTime(String creatTime) {
		this.creatTime = creatTime;
	}

	@Override
	public String toString() {
		return "CsdnBlogDetail{" +
				"title='" + title + '\'' +
				", pref='" + pref + '\'' +
				", url='" + url + '\'' +
				", context='" + context + '\'' +
				", blogId='" + blogId + '\'' +
				", readCount='" + readCount + '\'' +
				", creatTime='" + creatTime + '\'' +
				'}';
	}


}
