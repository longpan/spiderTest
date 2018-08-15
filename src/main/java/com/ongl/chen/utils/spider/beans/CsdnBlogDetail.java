
/**
* @Title: CsdnBlogDetail.java
* @Package com.ongl.chen.utils.spider.beans
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.beans;

/**
* @ClassName: CsdnBlogDetail
* @Description: TODO(这里用一句话描述这个类的作用)
* @author maojing.long
* @date 2018年8月15日
*
*/

public class CsdnBlogDetail {
	
	String title;
	
	String pref;
	
	String url;
	
	String context;
	
	String id;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "CsdnBlogDetail [title=" + title + ", pref=" + pref + ", url=" + url + ", context=" + context + ", id="
				+ id + "]";
	}
	
	

}
