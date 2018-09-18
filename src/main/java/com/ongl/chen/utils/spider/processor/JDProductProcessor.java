
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

    public static final int maxPageNum = 201; //100页

    @Autowired
    JDProductDetailPipline jdProductDetailPipline;


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

                productDetailList.add(productDetail);

//                page.addTargetRequest(url);
            }
            page.putField("product_detail_list", productDetailList);


            String pageUrl = page.getUrl().all().get(0).toString();
            String nextPageUrl = getNextPageUrl(pageUrl);

            if(nextPageUrl != null && !"".equals(nextPageUrl)) {
                typeMap.put(nextPageUrl, typeMap.get(page.getHtml().getDocument().baseUri()));
                page.addTargetRequest(nextPageUrl);

            }



        }

        System.out.println("end");



//        System.out.println(page.toString());
    }

    public Site getSite() {
        return site;
    }

    public String getNextPageUrl(String thisUrl) {

        Map<String, String> mapRequest = URLRequest(thisUrl);
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
        String baseUrl = getBaseUrl(thisUrl);

        String nextUrl =  constuctUrl(mapRequest, baseUrl);



        return nextUrl;

    }

    private String constuctUrl(Map<String, String> mapRequest, String baseUrl) {
        int paraseNum = 0;
        String url = baseUrl;
        for (Map.Entry<String, String> entry : mapRequest.entrySet()) {
            if(paraseNum == 0) {
                url = url + "?" + entry.getKey() + "=" + entry.getValue();
            }else {
                url = url + "&" + entry.getKey() + "=" + entry.getValue();
            }
            paraseNum ++;
        }
        return url;
    }

    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> URLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = TruncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        //每个键值为一组 www.2cto.com
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (arrSplitEqual[0] != "") {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }


    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    /**
     * 去掉请求参数部分，留下url中的路径
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String getBaseUrl(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 0) {
                if (arrSplit[0] != null) {
                    strAllParam = arrSplit[0];
                }
            }
        }


        return strAllParam;
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
            String url = indexUrl + "&keyword=" + keyWordEncode;
            typeMap.put(url, word);
        }
        System.out.println(typeMap.toString());
    }

    public void start() {
//        System.setProperty("selenuim_config", "/Users/apple/Proenv/selenium/config.ini");
        System.setProperty("selenuim_config", "/home/ubuntu/proenv/selenium_config.ini");
//        String indexUrl = "https://channel.jd.com/1319-1523.html";
        String indexUrl = "https://search.jd.com/Search?enc=utf-8&spm=2.1.0";

        String keyWords = "奶瓶奶嘴,牙胶安抚,食物存储,儿童餐具,围兜/防溅衣,水壶/水杯";


        String[] words = keyWords.split(",");
        List<String> urlList = new ArrayList<String>();


        for(String word : words) {
            System.out.println(word);
            String keyWordEncode = null;
            try {
                keyWordEncode = URLEncoder.encode( word, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = indexUrl + "&keyword=" + keyWordEncode;
            typeMap.put(url, word);
            urlList.add(url);
        }
//        String chromeDriverPath = "/usr/local/bin/chromedriver";
        String chromeDriverPath = "/usr/bin/chromedriver";


//        String indexUrl = "https://search.jd.com/Search?keyword=1%E6%AE%B5%E5%A5%B6%E7%B2%89&enc=utf-8&wq=1%E6%AE%B5%E5%A5%B6%E7%B2%89&pvid=wu368axi.eoe5m8";
//        String indexUrl = "https://search.jd.com/Search?keyword=2%E6%AE%B5%E5%A5%B6%E7%B2%89&enc=utf-8&spm=2.1.0";
        Spider.create(new JDProductProcessor()).addUrl(urlList.toArray(new String[urlList.size()])).addPipeline(jdProductDetailPipline).setDownloader(new JDSeleniuDownloader(chromeDriverPath)).thread(5).run();
    }
}
