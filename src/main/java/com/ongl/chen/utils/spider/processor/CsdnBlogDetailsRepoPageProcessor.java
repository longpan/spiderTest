
/**
* @Title: GithubRepoPageProcessor.java
* @Package com.ongl.chen.utils.spider.processor
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.processor;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import com.ongl.chen.utils.spider.beans.CsdnBlogDetail;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;
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

	public static final String URL_INDEX = "https://blog.csdn.net";

	public HashMap<String, CsdnBlogDetail> blogMap = new HashMap<String, CsdnBlogDetail>();
	
	
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
				
//				page.addTargetRequest(url);
				System.out.println(detail.toString());
				
				
			}
			System.out.println("11111");
		} else if ((page.getUrl()).regex(URL_DETAIL).match()){
			System.out.println("2222222");

			String id = getIdByUrl(page.getUrl().toString());
			CsdnBlogDetail blogDetail = blogMap.get(id);
			if(blogDetail == null) {
				return;
			}

			String createTime = page.getHtml().$(".article-info-box").xpath("//span[@class='time']/text()").toString();
			String readCounts = page.getHtml().$(".article-info-box").xpath("//span[@class='read-count']/text()").toString();
			//article_content
			String content = page.getHtml().$("#article_content").xpath("/allText()").toString();
			blogDetail.setReadCount(readCounts);
			blogDetail.setCreatTime(createTime);
//			blogDetail.setContext(content);

			System.out.println(blogDetail.toString());
			
		}else if (page.getUrl().toString().equals(URL_INDEX)) {
			System.out.println("333333");

			//今日最佳
			Selectable selectable = page.getHtml().$(".company_list");
			List<Selectable>  list = selectable.css("li").$(".company_name").nodes();
			for(Selectable item : list) {

				CsdnBlogDetail detail = new CsdnBlogDetail();

				String title = item.xpath("//a/text()").toString();
				String url = item.links().toString();
				String id = getIdByUrl(url);
				detail.setTitle(title);
				detail.setUrl(url);
				detail.setId(id);
				blogMap.put(id,detail);

				page.addTargetRequest(url);

				System.out.println(detail.toString());

			}

		}



//		page.getHtml().xpath(xpath)
		

	}

	public String getIdByUrl(String url) {
		if(url == null || "".equals(url)) {
			return "";
		}else {
			return url.substring(url.lastIndexOf("/") + 1);
		}
	}

    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new CsdnBlogDetailsRepoPageProcessor()).addUrl("https://blog.csdn.net").thread(1).run();
    }
}
