
/**
* @Title: GithubRepoPageProcessor.java
* @Package com.ongl.chen.utils.spider.processor
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.processor;

import com.ongl.chen.utils.spider.beans.CbgItem;
import com.ongl.chen.utils.spider.beans.JDProductDetail;
import com.ongl.chen.utils.spider.downloader.CbgSeleniuDownloader;
import com.ongl.chen.utils.spider.downloader.JDSeleniuDownloader;
import com.ongl.chen.utils.spider.downloader.LagouSeleniuDownloader;
import com.ongl.chen.utils.spider.pipline.CbgItemExcelPipline;
import com.ongl.chen.utils.spider.pipline.JDProductDetailPipline;
import com.ongl.chen.utils.spider.utils.FileUtil;
import com.ongl.chen.utils.spider.utils.UrlStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @ClassName: CbgMhxysyProcessor
* @Description: 藏宝阁 梦幻西游手游角色
* @author maojing.long
* @date 2018年8月15日
*
*/
public class CbgMhxysyProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);



    public static HashMap<String, String> typeMap = new HashMap<String, String>();

    private static final String pageParms = "page";
    private static final String keyWordPara = "keyword";


    public static final int maxPageNum = 60; //100页

    @Autowired
    CbgItemExcelPipline cbgItemExcelPipline;



    public void process(Page page) {

            System.out.println("item list");
            //获取左边分类信息
            List<Selectable> itemList = page.getHtml().$(".info").nodes();
            List<CbgItem> cbgItemList = new ArrayList<CbgItem>();

            System.out.println("size == " + itemList.size());

            for( Selectable item: itemList) {
                String wrapName = item.$(".name-wrap").xpath("span[@class='name']/text()").toString();
                String level = item.xpath("//span[@class='level']/text()").toString();
                String serverName = item.xpath("//span[@class='server-name']/text()").toString();
                String overallScore = item.xpath("//span[@class='basic_attrs_item'][1]/text()").toString();
                String personScore = item.xpath("//span[@class='basic_attrs_item'][2]/text()").toString();
                String price = item.xpath("//div[@class='price']/text()").toString();
                String txt = item.xpath("//div[@class='txt']/text()").toString();
                String collect = item.xpath("//div[@class='collect']/text()").toString();
                String bargin ="";
                if(item.$(".icon-bargin").toString() != null) {
                    bargin = "还";
                }
                String publicity = "";
                if(item.$(".icon-publicity").toString() != null) {
                    publicity = "公";
                }

//                String url = item.$(".p-img").links().toString();
//                String imgUrl = item.$(".p-img").xpath("//img/@src").toString();
//
//                String priceStr = item.$(".p-price").xpath("//i/text()").toString();
//                String pTag = item.$(".p-name").xpath("//span[@class='p-tag']/text()").toString();
//                String pName = item.$(".p-name").xpath("//em/text()").toString();
//                String pCommitNumStr = item.$(".p-commit").xpath("//a/text()").toString();
//
//                String  pShopName = item.$(".p-shop").xpath("//a/@title").toString();
//                String  pShopUrl = item.$(".p-shop").links().toString();
//
//                String pIcons = item.$(".p-icons").xpath("//i/text()").toString();
//                Double price = 0d;
//                try {
//                    price = Double.parseDouble(priceStr);
//                }catch (Exception e) {
//                    System.out.println("parse price err: " + priceStr);
//                }

                CbgItem cbgItem = new CbgItem();
                cbgItem.setWrapName(wrapName);
                cbgItem.setLevel(level);
                cbgItem.setPrice(price);
                cbgItem.setCollect(collect);
                cbgItem.setOverallScore(overallScore);
                cbgItem.setPersonScore(personScore);
                cbgItem.setServerName(serverName);
                cbgItem.setTxt(txt);
                cbgItem.setBargin(bargin);
                cbgItem.setPublicity(publicity);

                cbgItemList.add(cbgItem);
//                page.addTargetRequest(url);
            }


        page.putField("cbg_item_list", cbgItemList);
        //   page.putField("keyWord", keyWord);



//        System.out.println(page.toString());
    }



    public Site getSite() {
        return site;
    }





    public static void main(String[] args) {
        System.setProperty("selenuim_config", "/Users/onglchen/proenv/selenium/config.ini");
        String chromeDriverPath = "/usr/local/bin/chromedriver";
//        String chromeDriverPath = "/usr/bin/chromedriver";
       // Spider.create(new CbgMhxysyProcessor()).addUrl("https://my.cbg.163.com/cgi/mweb/pl?view_loc=equip_list&from=kingkong&tfid=f_kingkong&refer_sn=01933EA6-4505-655E-BD4F-92DF5539C411").setDownloader(new CbgSeleniuDownloader(chromeDriverPath)).thread(1).run();
        Spider.create(new CbgMhxysyProcessor()).addUrl("https://my.cbg.163.com/cgi/mweb/?refer_sn=019344AA-8DFB-4F14-C5C9-DC35F0348BE8").setDownloader(new CbgSeleniuDownloader(chromeDriverPath)).thread(1).run();

    }



    public void start() {
        System.setProperty("selenuim_config", "/Users/onglchen/proenv/selenium/config.ini");
        String indexUrl = "https://search.jd.com/Search?enc=utf-8&spm=2.1.0";

        String chromeDriverPath = "/usr/local/bin/chromedriver";
//        String chromeDriverPath = "/usr/bin/chromedriver";
        Spider.create(new CbgMhxysyProcessor()).addUrl("https://my.cbg.163.com/cgi/mweb/?refer_sn=019344AA-8DFB-4F14-C5C9-DC35F0348BE8").addPipeline(cbgItemExcelPipline).setDownloader(new CbgSeleniuDownloader(chromeDriverPath)).thread(1).run();

       // Spider.create(new CbgMhxysyProcessor()).addUrl("https://my.cbg.163.com/cgi/mweb/pl?view_loc=equip_list&from=kingkong&tfid=f_kingkong&refer_sn=01933EA6-4505-655E-BD4F-92DF5539C411").addPipeline(cbgItemExcelPipline).setDownloader(new CbgSeleniuDownloader(chromeDriverPath)).thread(1).run();
    }
}
