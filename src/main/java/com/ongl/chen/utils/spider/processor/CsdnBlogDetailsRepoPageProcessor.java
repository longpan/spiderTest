
/**
* @Title: GithubRepoPageProcessor.java
* @Package com.ongl.chen.utils.spider.processor
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.processor;

import java.util.List;

import com.ongl.chen.utils.spider.beans.CsdnBlogDetail;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

/**
* @ClassName: GithubRepoPageProcessor
* @Description: TODO(这里用一句话描述这个类的作用)
* @author maojing.long
* @date 2018年8月15日
*
*/

public class CsdnBlogDetailsRepoPageProcessor implements PageProcessor {

	private Site site = Site
			.me()
			.setDomain("blog.csdn.net")
			.setSleepTime(300)
			.setUserAgent(
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

	public static final String URL_LIST = "^http://blog.csdn.net/\\w+/article/list/[0-9]*[1-9][0-9]*$";
	
	public static final String URL_DETAIL = "https://blog.csdn.net/\\w+/article/details/\\w+";
	
	
	public void process(Page page) {
		// 列表页： 这里进行匹配，匹配出列表页进行相关处理。在列表页我们获取必要信息。对于全文、评论、顶、踩在文章详情中。
		if ((page.getUrl()).regex(URL_LIST).match()) {
			// 遍历出页码：遍历出div[@class=\"pagelist\"]节点下的所有超链接，该链接下是页码链接。将其加入到爬虫队列。【核心代码】
//			page.addTargetRequests(
//					page.getHtml().xpath("//div[@class=\"list_item_new\"]//div[@class=\"pagelist\"]").links().all());
//			
			List<Selectable> articleList = page.getHtml().$(".article-item-box").nodes();
			//page.getHtml().$(".article-item-box").nodes().get(0).links();
			for(Selectable article : articleList) {
				CsdnBlogDetail detail = new CsdnBlogDetail();
				
				String title = article.xpath("//a/text()").toString();
				String url = article.links().toString();
				detail.setTitle(title);
				detail.setUrl(url);
				
				page.addTargetRequest(url);
				System.out.println(detail.toString());
				
				
			}
			System.out.println("11111");
		} else if ((page.getUrl()).regex(URL_DETAIL).match()){
			System.out.println("2222222");
			page.getHtml().$(".article-info-box");
		}
		System.out.println("333333");
//		page.getHtml().xpath(xpath)
		

	}

    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new CsdnBlogDetailsRepoPageProcessor()).addUrl("http://blog.csdn.net/wgyscsf/article/list/1").thread(1).run();
    }
}
