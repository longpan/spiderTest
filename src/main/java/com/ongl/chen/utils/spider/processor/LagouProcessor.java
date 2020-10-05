
/**
* @Title: GithubRepoPageProcessor.java
* @Package com.ongl.chen.utils.spider.processor
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.processor;

import com.ongl.chen.utils.spider.LagouJobDetail;
import com.ongl.chen.utils.spider.downloader.LagouSeleniuDownloader;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;

/**
* @ClassName: BossProcessor
* @Description: boss直聘
* @author maojing.long
* @date 2020年10月5日
*
*/



public class LagouProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    public void process(Page page) {
        List<LagouJobDetail> jobList = new ArrayList<>();
        List<Selectable> jobItemList =  page.getHtml().xpath("//*[@id=\"s_position_list\"]/ul/li").nodes();
        for(Selectable jobItem : jobItemList) {
            String companyName = jobItem.xpath("li/@data-company").get();
            String postName = jobItem.xpath("li/@data-positionname").get();
            String salary = jobItem.xpath("li/@data-salary").get();
            String exp = jobItem.xpath("//*[@class='li_b_l']/text()").get();
            String industry = jobItem.xpath("//*[@class='industry']/text()").get();
            String benefit = jobItem.xpath("//*[@class='li_b_r']/text()").get();
            String area = jobItem.xpath("//*[@class='add']/text()").get();
            String publicTime = jobItem.xpath("//*[@class='format-time']/text()").get();
            String href = jobItem.xpath("//*[@class='position_link']/@href").get();
            //position_link
            LagouJobDetail lagouJobDetail =  LagouJobDetail.builder().area(area)
                                            .companyName(companyName).postName(postName).salary(salary).
                            exp(exp).industry(industry).benefit(benefit).publicTime(publicTime).href(href).build();
            jobList.add(lagouJobDetail);
        }
        System.out.println(jobList);
    }

    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        System.setProperty("selenuim_config", "/Users/apple/Proenv/selenium/config.ini");
        String chromeDriverPath = "/usr/local/bin/chromedriver";
//        String chromeDriverPath = "/usr/bin/chromedriver";

        Spider.create(new LagouProcessor()).addUrl("https://www.lagou.com/jobs/list_java/p-city_235?&cl=false&fromSearch=true&labelWords=&suginput=").setDownloader(new LagouSeleniuDownloader(chromeDriverPath)).thread(10).run();
    }
}
