
/**
* @Title: GithubRepoPageProcessor.java
* @Package com.ongl.chen.utils.spider.processor
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.processor.cbg;

import com.ongl.chen.utils.spider.beans.CbgItem;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;
import com.ongl.chen.utils.spider.downloader.CbgSeleniuDownloaderV3;
import com.ongl.chen.utils.spider.downloader.CbgSeleniuDownloaderV5;
import com.ongl.chen.utils.spider.downloader.cbg.CbgMhxySeleniuDownloader;
import com.ongl.chen.utils.spider.pipline.CbgItemExcelPipline;
import com.ongl.chen.utils.spider.service.CbgItemService;
import com.ongl.chen.utils.spider.service.MhPetItemService;
import com.ongl.chen.utils.spider.utils.AppConfig;
import com.ongl.chen.utils.spider.utils.AppConfigFromPost;
import com.ongl.chen.utils.spider.utils.UrlStringUtil;
import org.apache.commons.lang3.StringUtils;
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
* @ClassName: CbgMhxysyProcessor
* @Description: 藏宝阁 梦幻西游宠物
* @author maojing.long
* @date 2018年8月15日
*/
@Component
public class CbgMhxyProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    @Autowired
    private AppConfig appConfig;


    MhPetItemService mhPetItemService;

    String URL_INDEX = "https://xyq.cbg.163.com/cgi-bin/query.py?act=search_pet";

    private static final String pageParms = "page";
    private static final String keyWordPara = "keyword";

    public CbgMhxyProcessor(MhPetItemService mhPetItemService) {
        this.mhPetItemService = mhPetItemService;
    }

    public CbgMhxyProcessor() {
    }

    public static final int maxPageNum = 60; //100页

    public void process(Page page) {

        String pageUrl = page.getUrl().toString();
        if (StringUtils.contains(page.getUrl().toString(), URL_INDEX)) {
            System.out.println("petList page " + pageUrl);
            List<Selectable> petList = page.getHtml().$("#soldList").xpath("tr").nodes();
            for (Selectable petSelectable : petList) {

               String detailUrl =  petSelectable.links().all().get(0);
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

                MhPetItem mhPetItem = new MhPetItem();
                mhPetItem.setDetailUrl(detailUrl);
                mhPetItem.setPrice(price);
                mhPetItem.setCollect(collect);
                mhPetItem.setLightSpot1(lightSpot1Temp);
                mhPetItem.setLightSpot2(lightSpot2Temp);
                mhPetItemService.insertOrUpdateByDetailUrl(mhPetItem);

                page.addTargetRequest(detailUrl);
            }
            String nextPageUrl = getNextPageUrl(pageUrl);
            System.out.println("nextPageUrl : " + nextPageUrl);
            page.addTargetRequest(nextPageUrl);
        }
        if(StringUtils.contains(pageUrl, "xyq.cbg.163.com/equip?")) {
            MhPetItem mhPetItem = new MhPetItem();
            List<Selectable> infoList = page.getHtml().$(".infoList").nodes().get(0).xpath("li").nodes();
           String nameStr =  page.getHtml().$(".names").xpath("li/text()").toString();
            String name = StringUtils.split(nameStr, " ")[0];
            String level = StringUtils.split(nameStr, " ")[1];
            int skillNum = page.getHtml().$("#pet_skill_grid_con").xpath("//img").nodes().size();
            List<String> skillList = page.getHtml().$("#pet_skill_grid_con").xpath("//img/@data_store_name").all();
            System.out.println("petDetail page : " + pageUrl);
            //气血
            String blood = page.getHtml().$(".tb02").xpath("//tr[1]/td[1]/text()").toString();
            mhPetItem.setBlood(blood);
            //体质
            String corporeity = page.getHtml().$(".tb02").xpath("//tr[1]/td[2]/text()").toString();
            mhPetItem.setCorporeity(corporeity);
            //魔法
            String magic = page.getHtml().$(".tb02").xpath("//tr[2]/td[1]/text()").toString();
            mhPetItem.setMagic(magic);
            //法力
            String power = page.getHtml().$(".tb02").xpath("//tr[2]/td[2]/text()").toString();
            mhPetItem.setPower(power);
            //攻击
            String attack = page.getHtml().$(".tb02").xpath("//tr[3]/td[1]/text()").toString();
            mhPetItem.setAttack(attack);
            //力量
            String strength = page.getHtml().$(".tb02").xpath("//tr[3]/td[2]/text()").toString();
            mhPetItem.setStrength(strength);
            //防御
            String defense = page.getHtml().$(".tb02").xpath("//tr[4]/td[1]/text()").toString();
            mhPetItem.setDefense(defense);
            //耐力
            String endurance = page.getHtml().$(".tb02").xpath("//tr[4]/td[2]/text()").toString();
            mhPetItem.setEndurance(endurance);
            //速度
            String speed = page.getHtml().$(".tb02").xpath("//tr[5]/td[1]/text()").toString();
            mhPetItem.setSpeed(speed);
            //敏捷
            String agility = page.getHtml().$(".tb02").xpath("//tr[5]/td[2]/text()").toString();
            mhPetItem.setAgility(agility);
            //法伤
            String mageWound = page.getHtml().$(".tb02").xpath("//tr[6]/td[1]/text()").toString();
            mhPetItem.setMageWound(mageWound);
            //潜能
            String potency = page.getHtml().$(".tb02").xpath("//tr[6]/td[2]/text()").toString();
            mhPetItem.setPotency(potency);
            //法防
            String mageDefence = page.getHtml().$(".tb02").xpath("//tr[7]/td[1]/text()").toString();
            mhPetItem.setMageDefence(mageDefence);

            //攻击资质
            String attackQualification = page.getHtml().$(".petZiZhiTb").xpath("//tr[1]/td[1]/text()").toString();
            mhPetItem.setAttackQualification(attackQualification);
            //寿命
            String lifetime = page.getHtml().$(".petZiZhiTb").xpath("//tr[1]/td[2]/text()").toString();
            mhPetItem.setLifetime(lifetime);
            //防御资质
            String defenseQualification = page.getHtml().$(".petZiZhiTb").xpath("//tr[2]/td[1]/text()").toString();
            mhPetItem.setDefenseQualification(defenseQualification);
            //成长
            String growUp = page.getHtml().$(".petZiZhiTb").xpath("//tr[2]/td[2]/text()").toString();
            mhPetItem.setGrowUp(growUp);
            //体力资质
            String physicalQualification = page.getHtml().$(".petZiZhiTb").xpath("//tr[3]/td[1]/text()").toString();
            mhPetItem.setPhysicalQualification(physicalQualification);
            //法力资质
            String manaQualification = page.getHtml().$(".petZiZhiTb").xpath("//tr[4]/td[1]/text()").toString();
            mhPetItem.setManaQualification(manaQualification);
            //速度资质
            String speedQualification = page.getHtml().$(".petZiZhiTb").xpath("//tr[5]/td[1]/text()").toString();
            mhPetItem.setSpeedQualification(speedQualification);
            //躲闪资质
            String dodgeQualification = page.getHtml().$(".petZiZhiTb").xpath("//tr[6]/td[1]/text()").toString();
            mhPetItem.setDodgeQualification(dodgeQualification);
            //是否宝宝
            String isBaby = page.getHtml().$(".petZiZhiTb").xpath("//tr[7]/td[1]/text()").toString();
            mhPetItem.setIsBaby(isBaby);




            mhPetItem.setDetailUrl(pageUrl);
            mhPetItem.setLevel(level);
            mhPetItem.setName(name);
            mhPetItem.setSkillNum(skillNum);
            if(skillList != null) {
                mhPetItem.setSkillList(skillList.toString());
            }
            mhPetItemService.insertOrUpdateByDetailUrl(mhPetItem);



        }


    }

    public String getNextPageUrl(String thisUrl) {

        Map<String, String> mapRequest = UrlStringUtil.URLRequest(thisUrl);
        int pageNum = 1;
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
        Spider.create(new CbgMhxyProcessor(null)).addUrl("https://xyq.cbg.163.com/cgi-bin/query.py?act=search_pet").setDownloader(new CbgMhxySeleniuDownloader(chromeDriverPath)).thread(1).run();

    }



    public void start(AppConfigFromPost appConfigFromPost, MhPetItemService mhPetItemService) {
        System.setProperty("selenuim_config", appConfigFromPost.getSelenuimConfig());
        this.mhPetItemService = mhPetItemService;
        String chromeDriverPath = appConfigFromPost.getChromeDriverPath();
        CbgMhxySeleniuDownloader seleniuDownloader = new CbgMhxySeleniuDownloader(chromeDriverPath);

        Spider.create(new CbgMhxyProcessor(mhPetItemService)).addUrl("https://xyq.cbg.163.com/cgi-bin/query.py?act=search_pet").setDownloader(seleniuDownloader).thread(1).run();

       // Spider.create(new CbgMhxysyProcessor()).addUrl("https://my.cbg.163.com/cgi/mweb/pl?view_loc=equip_list&from=kingkong&tfid=f_kingkong&refer_sn=01933EA6-4505-655E-BD4F-92DF5539C411").addPipeline(cbgItemExcelPipline).setDownloader(new CbgSeleniuDownloader(chromeDriverPath)).thread(1).run();
    }
}
