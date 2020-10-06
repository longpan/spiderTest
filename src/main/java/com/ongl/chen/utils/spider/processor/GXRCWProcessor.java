
/**
* @Title: GithubRepoPageProcessor.java
* @Package com.ongl.chen.utils.spider.processor
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.processor;

import com.ongl.chen.utils.spider.beans.GXRFWJobDetail;
import com.ongl.chen.utils.spider.pipline.GXRCWExcelPipline;
import com.ongl.chen.utils.spider.utils.UrlStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @ClassName: GXRCWProcessor
* @Description: 广西人才网
* @author maojing.long
* @date 2020年10月5日
*
*/


@Component
public class GXRCWProcessor implements PageProcessor {

    private static final String pagePara = "page";
    //keyword
    private static final String keywordPara = "keyword";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    public static final int maxPageNum = 33; //20页

    @Autowired
    private GXRCWExcelPipline gxrcwExcelPipline;

    List<GXRFWJobDetail> allJobList = new ArrayList<>();

    public void process(Page page) {
        String keyWord = getKeyWordByUrl(page.getUrl().toString());
        List<GXRFWJobDetail> jobList = new ArrayList<>();
        List<Selectable> jobItemList =  page.getHtml().xpath("//*[@class=\"rlOne\"]").nodes();
        for(Selectable jobItem : jobItemList) {
            String companyName = jobItem.xpath("//*[@class='entName']/text()").get();
            String postName = jobItem.xpath("//*[@class='posName']/text()").get();
            String salary = jobItem.xpath("//*[@class='w3']/text()").get();
            String exp = jobItem.xpath("//*[@class='qitaUL']/li[2]/span/text()").get();
            String companyAptitude = jobItem.xpath("//*[@class='qitaUL']/li[4]/span/text()").get();
            String bref = jobItem.xpath("//*[@class='posInfo']/text()").get();
            String benefit = jobItem.xpath("//*[@class='li_b_r']/text()").get();
            String area = jobItem.xpath("//*[@class='w4']/text()").get();
            String publicTime = jobItem.xpath("//*[@class='w5']/text()").get();
            String href = jobItem.xpath("//*[@class='w1']/h3/a/@href").get();

            GXRFWJobDetail lagouJobDetail =  GXRFWJobDetail.builder().area(area)
                                            .companyName(companyName).postName(postName).salary(salary).
                            exp(exp).keyWord(keyWord).companyAptitude(companyAptitude).bref(bref).benefit(benefit).publicTime(publicTime).href(href).build();
            jobList.add(lagouJobDetail);
        }
        System.out.println(jobList.size());

        allJobList.addAll(jobList);

        //下一页
        String pageUrl = page.getUrl().all().get(0).toString();
        String nextPageUrl = getNextPageUrl(pageUrl, jobList.size());

        if(nextPageUrl != null && !"".equals(nextPageUrl)) {
            page.addTargetRequest(nextPageUrl);

        }else {

            page.putField("job_list", allJobList);
            page.putField("keyWord", keyWord);
        }
    }

    public Site getSite() {
        return site;
    }

    public void start(String keyWord) {
        Spider.create(new GXRCWProcessor()).addUrl("https://s.gxrc.com/sJob?keyword=" + keyWord + "&schType=1&page=1").addPipeline(gxrcwExcelPipline).thread(10).run();

    }

    public static void main(String[] args) {
        System.setProperty("selenuim_config", "/Users/apple/Proenv/selenium/config.ini");
        String chromeDriverPath = "/usr/local/bin/chromedriver";
//        String chromeDriverPath = "/usr/bin/chromedriver";

        Spider.create(new GXRCWProcessor()).addUrl("https://s.gxrc.com/sJob?keyword=java&schType=1&page=2").thread(1).run();
    }

    public static String getKeyWordByUrl(String url) {
        Map<String, String> paraMap = UrlStringUtil.URLRequest(url);

        return paraMap.get(keywordPara);
    }

    public String getNextPageUrl(String thisUrl, int count) {

        Map<String, String> mapRequest = UrlStringUtil.URLRequest(thisUrl);
        int pageNum = 1;
        if (mapRequest.containsKey(pagePara)) {
            String pageStr = mapRequest.get(pagePara);
            int page = Integer.parseInt(pageStr);
            page = page + 1;
            mapRequest.put(pagePara, page + "");

//            //大于100页就停止
//            if(page >= maxPageNum) {
//                return  "";
//            }
            //如果当前页没有数据，则停止
            if(count <= 0) {
                return "";
            }
        }else {
            mapRequest.put(pagePara, "3");
        }
        String baseUrl = UrlStringUtil.getBaseUrl(thisUrl);

        String nextUrl =  UrlStringUtil.constuctUrl(mapRequest, baseUrl);



        return nextUrl;

    }

}
