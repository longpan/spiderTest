
/**
* @Title: GithubRepoPageProcessor.java
* @Package com.ongl.chen.utils.spider.processor
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2018年8月15日
*/
package com.ongl.chen.utils.spider.processor;

import com.ongl.chen.utils.spider.beans.JDProductDetail;
import com.ongl.chen.utils.spider.downloader.JDSeleniuDownloader;
import com.ongl.chen.utils.spider.pipline.JDProductDetailPipline;
import com.ongl.chen.utils.spider.utils.FileUtil;
import com.ongl.chen.utils.spider.utils.UrlStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
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
* @ClassName: GithubRepoPageProcessor
* @Description: TODO(这里用一句话描述这个类的作用)
* @author maojing.long
* @date 2018年8月15日
*
*/
@Component
public class JDProductProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    public static final String URL_ITEM_LIST = "https://search.jd.com/Search\\?";

    public static final String URL_INDEX2 = "https://channel.jd.com/\\d{4}-\\d{4}.html";

    public static final String URL_DETAIL = "https://blog.csdn.net/\\w+/article/details/\\w+";

    public static final String URL_INDEX = "https://baby.jd.com/";

    public static HashMap<String, String> typeMap = new HashMap<String, String>();

    private static final String pageParms = "page";
    private static final String keyWordPara = "keyword";


    public static final int maxPageNum = 60; //100页

    @Autowired
    JDProductDetailPipline jdProductDetailPipline;


    public void process(Page page) {

        String keyWord = getKeyWordByUrl(page.getUrl().toString());
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
            List<JDProductDetail> productDetailList = new ArrayList<JDProductDetail>();

            System.out.println("size == " + itemList.size());

            String pType = typeMap.get(page.getHtml().getDocument().baseUri());

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
                Double price = 0d;
                try {
                    price = Double.parseDouble(priceStr);
                }catch (Exception e) {
                    System.out.println("parse price err: " + priceStr);
                }

                int commitNum = FileUtil.parseCommitNum(pCommitNumStr);

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

                productDetail.setType(pType);
                productDetail.setPId(FileUtil.getIdByUrl(url));

                productDetail.setPrice(price);
                productDetail.setCommitNum(commitNum);


                productDetailList.add(productDetail);

//                page.addTargetRequest(url);
            }


            String pageUrl = page.getUrl().all().get(0).toString();
            String nextPageUrl = getNextPageUrl(pageUrl);

            if(nextPageUrl != null && !"".equals(nextPageUrl)) {
                typeMap.put(nextPageUrl, typeMap.get(page.getHtml().getDocument().baseUri()));
                page.addTargetRequest(nextPageUrl);

            }else {
                page.putField("product_detail_list", productDetailList);
                page.putField("keyWord", keyWord);
            }



        }



//        System.out.println(page.toString());
    }



    public Site getSite() {
        return site;
    }

    public String getNextPageUrl(String thisUrl) {

        Map<String, String> mapRequest = UrlStringUtil.URLRequest(thisUrl);
        int pageNum = 1;
        if (mapRequest.containsKey(pageParms)) {
            String pageStr = mapRequest.get(pageParms);
            int page = Integer.parseInt(pageStr);
            page = page + 2;
            mapRequest.put(pageParms, page + "");

            //大于100页就停止
            if(page >= maxPageNum) {
                return  "";
            }
        }else {
            mapRequest.put(pageParms, "3");
        }
        String baseUrl = UrlStringUtil.getBaseUrl(thisUrl);

        String nextUrl =  UrlStringUtil.constuctUrl(mapRequest, baseUrl);



        return nextUrl;

    }




    public static void main(String[] args) {

//        new JDProductProcessor().start();
//        String result = null;
//        try {
//            result = URLEncoder.encode("2段奶粉", "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        String keyWords = "暖奶消毒,辅食料理机,吸奶器";

        String[] words = keyWords.split(",");
        String indexUrl = "https://search.jd.com/Search?enc=utf-8&spm=2.1.0";


        for(String word : words) {
            System.out.println(word);
            String keyWordEncode = null;
            try {
                keyWordEncode = URLEncoder.encode( word, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = indexUrl + "&" + keyWordPara + "=" + keyWordEncode;
            typeMap.put(url, word);
        }
        System.out.println(typeMap.toString());
    }

    public String getKeyWordByUrl(String url) {
        Map<String, String> paraMap = UrlStringUtil.URLRequest(url);
        String keyWordEncode = paraMap.get(keyWordPara);
        try {
            String keyWordDecode = URLDecoder.decode( keyWordEncode, "utf-8");
            return keyWordDecode;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void start(String keyWord) {
        System.setProperty("selenuim_config", "/Users/apple/Proenv/selenium/config.ini");
//        System.setProperty("selenuim_config", "/home/ubuntu/proenv/selenium_config.ini");
//        String indexUrl = "https://channel.jd.com/1319-1523.html";
        String indexUrl = "https://search.jd.com/Search?enc=utf-8&spm=2.1.0";

//        String keyWords = "1段,2段,3段,4段,孕妈奶粉,特殊配方奶粉,有机奶粉,米粉/菜粉,面条/粥,果汁/果泥,益生菌/初乳,DHA,钙铁锌/维生素,宝宝零食,清火/开胃,拉拉裤,成人尿裤,婴儿湿巾,洗发沐浴,洗澡用具,洗衣液/皂,座便器,宝宝护肤,日常护理,理发器,婴儿口腔清洁,驱蚊防晒,婴儿推车,安全座椅,提篮式,增高垫,婴儿床,婴儿床垫,餐椅摇椅,童乐车,电动车,自行车,三轮车,滑板车,扭扭车,学步车";


        List<String> urlList = new ArrayList<String>();

            System.out.println(keyWord);
            String keyWordEncode = null;
            try {
                keyWordEncode = URLEncoder.encode( keyWord, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = indexUrl + "&" + keyWordPara + "=" + keyWordEncode;

            typeMap.put(url, keyWord);
            urlList.add(url);
        String chromeDriverPath = "/usr/local/bin/chromedriver";
//        String chromeDriverPath = "/usr/bin/chromedriver";


//        String indexUrl = "https://search.jd.com/Search?keyword=1%E6%AE%B5%E5%A5%B6%E7%B2%89&enc=utf-8&wq=1%E6%AE%B5%E5%A5%B6%E7%B2%89&pvid=wu368axi.eoe5m8";
//        String indexUrl = "https://search.jd.com/Search?keyword=2%E6%AE%B5%E5%A5%B6%E7%B2%89&enc=utf-8&spm=2.1.0";
        Spider.create(new JDProductProcessor()).addUrl(urlList.toArray(new String[urlList.size()])).addPipeline(jdProductDetailPipline).setDownloader(new JDSeleniuDownloader(chromeDriverPath)).thread(10).run();
    }
}
