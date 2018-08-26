
/**
* @Title: GithubRepoPageProcessor.java
* @Package com.ongl.chen.utils.spider.processor
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.processor;

import com.ongl.chen.utils.spider.beans.JDProductDetail;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.HashMap;
import java.util.List;

/**
* @ClassName: GithubRepoPageProcessor
* @Description: TODO(这里用一句话描述这个类的作用)
* @author maojing.long
* @date 2018年8月15日
*
*/

public class JDProductProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    public static final String URL_ITEM_LIST = "https://search.jd.com/Search\\?";

    public static final String URL_INDEX2 = "https://channel.jd.com/\\d{4}-\\d{4}.html";

    public static final String URL_DETAIL = "https://blog.csdn.net/\\w+/article/details/\\w+";

    public static final String URL_INDEX = "https://baby.jd.com/";

    private HashMap<String, String> typeMap = new HashMap<String, String>();


    public void process(Page page) {

        //母婴首页
        if (page.getUrl().toString().equals(URL_INDEX)) {

            //获取左边分类信息
            List<Selectable> itemList = page.getHtml().xpath("//div[@id='baby_banner_2']").$(".baby_nav_body").$(".item").nodes();

            //只取大分类
            for( Selectable item: itemList) {
                String typeName = item.$(".item_header").xpath("//a/text()").toString();
                String url = item.$(".item_header").links().all().get(0);
                typeMap.put(url, typeName);
                System.out.println("type = " + typeName + ", url = " + url);

                page.addTargetRequest(url);
            }

            System.out.println("index");
        }
        //奶粉首页
        if ((page.getUrl()).regex(URL_INDEX2).match()) {

            System.out.println("index2");
            //获取左边分类信息
            List<Selectable> itemList = page.getHtml().$(".nf_cate_bd").$(".nf_cate_item").nodes();

            for( Selectable item: itemList) {
                String typeName = item.xpath("//a/@title").toString();
                String url = item.links().toString();
                typeMap.put(url, typeName);
                System.out.println("type = " + typeName + ", url = " + url);

                page.addTargetRequest(url);
            }
        }

        //奶粉列表页
        if ((page.getUrl()).regex(URL_ITEM_LIST).match()) {

            System.out.println("item list");
            //获取左边分类信息
            List<Selectable> itemList = page.getHtml().$("#J_goodsList").css("li").nodes();

            for( Selectable item: itemList) {
                String url = item.$(".p-img").links().toString();
                String imgUrl = item.$(".p-img").xpath("//img/@src").toString();

                String priceStr = item.$(".p-price").xpath("//i/text()").toString();
                String pTag = item.$(".p-name").xpath("//span[@class='p-tag']/text()").toString();
                String pName = item.$(".p-name").xpath("//em/text()").toString();
                String pCommitNumStr = item.$(".p-commit").xpath("//a/text()").toString();

                String  pShopName = item.$(".p-shop").xpath("//a/@title").toString();
                String  pShopUrl = item.$(".p-shop").links().toString();

                String pIcons = item.$(".p-icons").xpath("//i/text()").toString();

                JDProductDetail productDetail = new JDProductDetail();
                productDetail.setUrl(url);
                productDetail.setImgUrl(imgUrl);
                productDetail.setPriceStr(priceStr);
                productDetail.setpTag(pTag);
                productDetail.setpName(pName);
                productDetail.setpCommitNumStr(pCommitNumStr);
                productDetail.setpShopName(pShopName);
                productDetail.setpShopUrl(pShopUrl);
                productDetail.setpIcons(pIcons);

                System.out.println(productDetail.toString());

//                page.addTargetRequest(url);
            }
        }

        System.out.println("end");
        
//        System.out.println(page.toString());
    }

    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        System.setProperty("selenuim_config", "/Users/apple/Proenv/selenium/config.ini");
        String indexUrl = "https://channel.jd.com/1319-1523.html";
//        String index = "https://search.jd.com/Search?keyword=1%E6%AE%B5%E5%A5%B6%E7%B2%89&enc=utf-8&wq=1%E6%AE%B5%E5%A5%B6%E7%B2%89&pvid=wu368axi.eoe5m8";
        Spider.create(new JDProductProcessor()).addUrl(indexUrl).setDownloader(new SeleniumDownloader("/usr/local/bin/chromedriver")).thread(1).run();
    }
}
