
/**
* @Title: GithubRepoPageProcessor.java
* @Package com.ongl.chen.utils.spider.processor
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.processor.cbg;

import com.ongl.chen.utils.spider.beans.dbg.MhEquipItem;
import com.ongl.chen.utils.spider.downloader.cbg.CbgMhxySeleniuDownloader;
import com.ongl.chen.utils.spider.service.MhEquipItemService;
import com.ongl.chen.utils.spider.service.MhValuationService;
import com.ongl.chen.utils.spider.utils.AppConfig;
import com.ongl.chen.utils.spider.utils.AppConfigFromPostForCbg;
import com.ongl.chen.utils.spider.utils.UrlStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import java.util.List;
import java.util.Map;

/**
* @ClassName: CbgMhxysyProcessor
* @Description: 藏宝阁 梦幻西游灵饰
* @author maojing.long
* @date 2018年8月15日
*/
@Component
public class CbgMhxyEquipProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);


    @Autowired
    private MhValuationService mhValuationService;


    MhEquipItemService mhEquipItemService;

    AppConfigFromPostForCbg appConfigFromPostForCbg;

    String URL_INDEX = "https://xyq.cbg.163.com/cgi-bin/query.py?act=search_role_equip";

    private static final String pageParms = "page";

    private final Set<String> seenDetailUrls = Collections.synchronizedSet(new HashSet<>());

    public CbgMhxyEquipProcessor(MhEquipItemService mhEquipItemService, AppConfigFromPostForCbg appConfigFromPostForCbg) {
        this.mhEquipItemService = mhEquipItemService;
        this.appConfigFromPostForCbg = appConfigFromPostForCbg;
        this.maxPageNum = appConfigFromPostForCbg.getMaxPage();
    }

    public CbgMhxyEquipProcessor() {
    }

    public int maxPageNum = 60; //100页

    public void process(Page page) {

        String pageUrl = page.getUrl().toString();
        if (StringUtils.contains(page.getUrl().toString(), URL_INDEX)) {
            System.out.println("petList page " + pageUrl);
            List<Selectable> petList = page.getHtml().$("#soldList").xpath("tr").nodes();
            for (Selectable petSelectable : petList) {

               String detailUrl =  petSelectable.links().all().get(0);
                if (StringUtils.isBlank(detailUrl) || !seenDetailUrls.add(detailUrl)) {
                    continue;
                }
                String price = petSelectable.xpath("//td[5]/span/text()").toString();
                List<Selectable> lightSpot1Selectable =   petSelectable.xpath("//td[3]/a").nodes();
                String lightSpot1Temp = "";
                for(int index = 1; index <= lightSpot1Selectable.size(); index ++) {
                    lightSpot1Temp = lightSpot1Temp + petSelectable.xpath("//td[3]/a[" + index + "]/text()").toString() + ",";
                }

                List<Selectable> lightSpot2Selectable =   petSelectable.xpath("//td[4]/span").nodes();
                String lightSpot2Temp = "";
                for(int index = 1; index <= lightSpot2Selectable.size(); index ++) {
                    lightSpot2Temp = lightSpot2Temp + petSelectable.xpath("//td[4]/span[" + index + "]/text()").toString() + ",";
                }
                String collect = petSelectable.xpath("//td[8]/div/text()").toString();
                System.out.println("price = " + price);
                System.out.println("lightSpot1Temp = " + lightSpot1Temp);
                System.out.println("lightSpot2Temp = " + lightSpot2Temp);

                String name = petSelectable.xpath("//td[@class='equip_name']/div[1]/span/text()").toString();
                String level = petSelectable.xpath("//td[@class='equip_name']/div[2]/text()").toString();
                MhEquipItem mhEquipItem = new MhEquipItem();
                mhEquipItem.setDetailUrl(detailUrl);
                mhEquipItem.setPrice(price);
                mhEquipItem.setCollect(collect);
                mhEquipItem.setLightSpot1(lightSpot1Temp);
                mhEquipItem.setLightSpot2(lightSpot2Temp);
                mhEquipItem.setName(name);
                mhEquipItem.setLevel(level);
                int insertRes = mhEquipItemService.insertOrUpdateByDetailUrl(mhEquipItem);
                if (insertRes == 1) {
                    page.addTargetRequest(detailUrl);
                }
            }
            String nextPageUrl = getNextPageUrl(pageUrl);
            System.out.println("nextPageUrl : " + nextPageUrl);
            page.addTargetRequest(nextPageUrl);
        }
        if(StringUtils.contains(pageUrl, "xyq.cbg.163.com/equip?")) {
            MhEquipItem mhEquipItem = new MhEquipItem();

            // 解析大区和服务器
            String serverInfo = page.getHtml().xpath("//div[@class='userInfo']/p[1]/text()").toString();
            if (StringUtils.isNotBlank(serverInfo) && serverInfo.contains("->")) {
                String[] parts = serverInfo.split("->");
                if (parts.length >= 2) {
                    mhEquipItem.setArea(parts[0].trim());
                    mhEquipItem.setServerName(parts[1].trim());
                }
            }

            String primeAttribute = page.getHtml().xpath("p[@id='equip_desc_panel']/span[@class='equip_desc_yellow']/text()").toString();
            List<String> accessoryAttributeList = page.getHtml().xpath("//p[@id='equip_desc_panel']/span[@class='equip_desc_green']/text()").all();
            int accessoryAttributeNum = accessoryAttributeList.size();
            String accessoryAttribute = accessoryAttributeList.toString();
            String specialEffects = page.getHtml().xpath("//p[@id='equip_desc_panel']/span[@style='color:#4DBAF4']/text()").all().toString();
            String runestones = page.getHtml().xpath("//p[@id='equip_desc_panel']/span[@style='color:#EE82EE']/text()").toString();

            String descYellow = page.getHtml().xpath("p[@id='equip_desc_panel']/span[@class='equip_desc_yellow']/text()").all().toString();
            mhEquipItem.setPrimeAttribute(primeAttribute);
            mhEquipItem.setAccessoryAttributeNum(accessoryAttributeNum);
            mhEquipItem.setAccessoryAttribute(accessoryAttribute);
            mhEquipItem.setSpecialEffects(specialEffects);
            mhEquipItem.setDetailUrl(pageUrl);
            mhEquipItem.setRunestones(runestones);
            mhEquipItem.setDescYellow(descYellow);
            
            if (mhValuationService != null) {
                mhValuationService.valuateEquip(mhEquipItem);
            }

            mhEquipItemService.insertOrUpdateByDetailUrl(mhEquipItem);



        }


    }

    public String getNextPageUrl(String thisUrl) {

        Map<String, String> mapRequest = UrlStringUtil.URLRequest(thisUrl);
        if (mapRequest.containsKey(pageParms)) {
            String pageStr = mapRequest.get(pageParms);
            int page = Integer.parseInt(pageStr);
            page = page + 1;
            mapRequest.put(pageParms, page + "");

            //大于100页就停止
            if(page >= maxPageNum) {
                return  "";
            }
        }else {
            mapRequest.put(pageParms, "2");
        }
        String baseUrl = UrlStringUtil.getBaseUrl(thisUrl);

        String nextUrl =  UrlStringUtil.constuctUrl(mapRequest, baseUrl);



        return nextUrl;

    }

    public Site getSite() {
        return site;
    }





    public static void main(String[] args) {
        System.setProperty("selenuim_config", "/Users/onglchen/proenv/selenium/config.ini");
        String chromeDriverPath = "/usr/local/bin/chromedriver";
//        String chromeDriverPath = "/usr/bin/chromedriver";
       // Spider.create(new CbgMhxysyProcessor()).addUrl("https://my.cbg.163.com/cgi/mweb/pl?view_loc=equip_list&from=kingkong&tfid=f_kingkong&refer_sn=01933EA6-4505-655E-BD4F-92DF5539C411").setDownloader(new CbgSeleniuDownloader(chromeDriverPath)).thread(1).run();
        Spider.create(new CbgMhxyEquipProcessor(null, null)).addUrl("https://xyq.cbg.163.com/cgi-bin/query.py?act=search_pet").setDownloader(new CbgMhxySeleniuDownloader(chromeDriverPath, null)).thread(1).run();

    }



    public void start(AppConfigFromPostForCbg appConfigFromPost, MhEquipItemService mhEquipItemService) {
        System.setProperty("selenuim_config", appConfigFromPost.getSelenuimConfig());
        System.setProperty("headless", String.valueOf(appConfigFromPost.isHeadlessMode()));
        this.mhEquipItemService = mhEquipItemService;
        String chromeDriverPath = appConfigFromPost.getChromeDriverPath();
        this.appConfigFromPostForCbg = appConfigFromPost;
        CbgMhxySeleniuDownloader seleniuDownloader = new CbgMhxySeleniuDownloader(chromeDriverPath, appConfigFromPost);

        String detailUrl = StringUtils.strip(StringUtils.trim(appConfigFromPost.getDetailUrl()), "` ");
        Spider.create(new CbgMhxyEquipProcessor(mhEquipItemService, appConfigFromPost)).addUrl(detailUrl).setDownloader(seleniuDownloader).thread(1).run();

       // Spider.create(new CbgMhxysyProcessor()).addUrl("https://my.cbg.163.com/cgi/mweb/pl?view_loc=equip_list&from=kingkong&tfid=f_kingkong&refer_sn=01933EA6-4505-655E-BD4F-92DF5539C411").addPipeline(cbgItemExcelPipline).setDownloader(new CbgSeleniuDownloader(chromeDriverPath)).thread(1).run();
    }
}
