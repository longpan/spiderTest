package com.ongl.chen.utils.spider.downloader;

import com.ongl.chen.utils.spider.beans.CbgItem;
import com.ongl.chen.utils.spider.service.CbgItemService;
import com.ongl.chen.utils.spider.utils.AppConfigFromPost;
import com.ongl.chen.utils.spider.utils.ConstantUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by apple on 2018/8/29.

 v5:直接将详情存数据库中
 */

public class CbgSeleniuDownloaderV5 implements Downloader, Closeable {
    public volatile MyWebDriverPool webDriverPool;


    private int sleepTime = 2000;

    private int poolSize = 1;

    private static final String DRIVER_PHANTOMJS = "phantomjs";

    private AppConfigFromPost appConfigFromPost;

    private CbgItemService cbgItemService;


    /**
     * 新建
     *
     * @param chromeDriverPath chromeDriverPath
     */
    public CbgSeleniuDownloaderV5(String chromeDriverPath) {
        System.getProperties().setProperty("webdriver.chrome.driver",
                chromeDriverPath);
    }

    /**
     * 新建
     *
     * @param chromeDriverPath chromeDriverPath
     */
    public CbgSeleniuDownloaderV5(String chromeDriverPath, AppConfigFromPost appConfigFromPost) {
        System.getProperties().setProperty("webdriver.chrome.driver",
                chromeDriverPath);
        this.appConfigFromPost = appConfigFromPost;
    }

    public CbgSeleniuDownloaderV5(String chromeDriverPath, AppConfigFromPost appConfigFromPost, CbgItemService cbgItemService) {
        System.getProperties().setProperty("webdriver.chrome.driver",
                chromeDriverPath);
        this.appConfigFromPost = appConfigFromPost;
        this.cbgItemService = cbgItemService;
    }
    /**
     * Constructor without any filed. Construct PhantomJS browser
     *
     * @author bob.li.0718@gmail.com
     */
    public CbgSeleniuDownloaderV5() {
        // System.setProperty("phantomjs.binary.path",
        // "/Users/Bingo/Downloads/phantomjs-1.9.7-macosx/bin/phantomjs");
    }

    /**
     * set sleep time to wait until load success
     *
     * @param sleepTime sleepTime
     * @return this
     */
    public CbgSeleniuDownloaderV5 setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    @SneakyThrows
    public Page download(Request request, Task task) {
        checkInit();
        WebDriver webDriver;
        Random random = new Random();
        try {
            webDriver = webDriverPool.get();
        } catch (InterruptedException e) {
            return null;
        }
        webDriver.get(request.getUrl());
                                                        //v2.s.2pT8N8wRrXYfWO_VIROZjt6GMGlzyzKQZLlh3uNpwDFBxQVg


        //将页面滚动条拖到底部
            int step = 13000;
            int start_y = 500;
            Actions actions = new Actions(webDriver);
            for (int index = 0; index < appConfigFromPost.getMaxPullDownCount(); index ++) {
                int postion_y = start_y + step * index;
                ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(0," + postion_y + ");");
//            ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(0,document.body.scrollHeight);");
                //actions.click().build().perform();
                try {
                    System.out.println(index);
                    Thread.sleep(appConfigFromPost.getPullDownSleepTimeMillis() + random.nextInt(2000));

                    if(StringUtils.contains(webDriver.getCurrentUrl(), "show_login")){
                        Thread.sleep(1000*60*60);
                        //doLoginV2(webDriver, random);
                    }

                    System.out.println(webDriver.findElements(By.className("info-wrap")).size());
                    if(webDriver.findElements(By.className("info-wrap")).size() > appConfigFromPost.getMaxItemCount()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }





        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebDriver.Options manage = webDriver.manage();

        Site site = task.getSite();
        if (site.getCookies() != null) {
            for (Map.Entry<String, String> cookieEntry : site.getCookies()
                    .entrySet()) {
                Cookie cookie = new Cookie(cookieEntry.getKey(),
                        cookieEntry.getValue());
                manage.addCookie(cookie);
            }
        }

		/*
		 * TODO You can add mouse event or other processes
		 *
		 * @author: bob.li.0718@gmail.com
		 */

        WebElement webElement = webDriver.findElement(By.xpath("/html"));
        String content = webElement.getAttribute("outerHTML");
        Page page = new Page();
        page.setRawText(content);
        page.setHtml(new Html(content, request.getUrl()));
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);

//        //解析列表
//        List<Selectable> itemList = page.getHtml().$(".info").nodes();
//        List<CbgItem> cbgItemList = new ArrayList<CbgItem>();
//
//        System.out.println("size == " + itemList.size());


       // Actions actions = new Actions(webDriver);

        try {
            WebElement btnCloseElement = webDriver.findElement(By.className("btn-close"));

            // 使用Actions类模拟鼠标点击
            actions.click(btnCloseElement).build().perform();
        }catch (NoSuchElementException e) {

        }

        List<WebElement> itemListElement = webDriver.findElements(By.className("info-wrap"));

//        for(int index = 0; index < itemListElement.size(); index ++) {
//            List<WebElement> itemListElementTemp = webDriver.findElements(By.className("info-wrap"));
//            WebElement itemElement = itemListElementTemp.get(index);
//            actions.click(itemElement).build().perform();
//            String detail = webDriver.getCurrentUrl();
//            System.out.println(detail);
//            detaiUrlList.add(detail);
//        }
//        WebElement itemElement = itemListElement.get(0);
        int index = 0;
        int insertNumbers = 0;
        int emptyNumbers = 0;
        int repeatNumbers = 0;
        for(WebElement itemElement : itemListElement) {

            try{
                CbgItem cbgItem = new CbgItem();


                if(StringUtils.contains( webDriver.getCurrentUrl(), "cgi/mweb/?")){
                    //点击进入详情页面
                    actions.click(itemElement).build().perform();
                    Thread.sleep(appConfigFromPost.getGetDetailUrlSleepTimeMillis() + random.nextInt(2000));
                }

                String detailUrl = webDriver.getCurrentUrl();
                System.out.println(detailUrl);
                System.out.println(index);
                index++;
                cbgItem.setDetailUrl(detailUrl);
                if(StringUtils.contains(detailUrl, "cgi/activity")){
                    webDriver.navigate().back();
                }

                if(StringUtils.contains(detailUrl, "show_login")){
                    Thread.sleep(1000*60*60);
                    //doLoginV2(webDriver, random);
                }

                    if(StringUtils.contains(detailUrl, "equip")) {

                    //解析详情页面
                    cbgItem.setCode(webDriver.findElement(By.className("pageProductDetail")).getAttribute("ordersn"));
                    cbgItem.setServerId(webDriver.findElement(By.className("pageProductDetail")).getAttribute("serverid"));
                    cbgItem.setWrapName(webDriver.findElement(By.xpath("//div[@class='name']")).getText());
                    cbgItem.setLevel(webDriver.findElement(By.xpath("//span[@class='level']")).getText());
                    try {
                        //收藏人数无法定位
                        String collect = webDriver.findElement(By.xpath("//div[@class='row']/span[3]")).getText();
                        if(StringUtils.contains(collect, "收藏")) {
                            cbgItem.setCollect(collect);
                        }
                    }catch (NoSuchElementException e) {

                    }

                    cbgItem.setOverallScore(webDriver.findElement(By.xpath("//span[@class='basic_attrs_item']")).getText());
                    cbgItem.setPersonScore(webDriver.findElement(By.xpath("//span[@class='basic_attrs_item'][2]")).getText());
                    cbgItem.setPrice(webDriver.findElement(By.xpath("//div[@class='price']")).getText());
                    cbgItem.setServerName(webDriver.findElement(By.xpath("//span[@class='area-server icon-text']")).getText());

                    try{
                        cbgItem.setTxt(webDriver.findElement(By.xpath("//div[@class='txt']")).getText());
                    }catch (NoSuchElementException e) {

                    }

                    String bargin ="";
                    try{
                        webDriver.findElement(By.className("icon-bargin"));
                        bargin = "还";
                    }catch (NoSuchElementException e) {

                    }
                    cbgItem.setBargin(bargin);

                    String publicity = "";
                    try {
                        webDriver.findElement(By.className("icon-publicity"));
                        publicity = "公";
                    }catch (NoSuchElementException e) {

                    }
                    cbgItem.setPublicity(publicity);

                    String draw ="";
                    try{
                        webDriver.findElement(By.className("icon-draw"));
                        draw = "抽";
                    }catch (NoSuchElementException e) {

                    }
                    cbgItem.setDraw(draw);

                    if(StringUtils.isEmpty(cbgItem.getWrapName())) {
                        emptyNumbers ++;
                        System.out.println("emptyNumbers : " + emptyNumbers);
                        //跳转回列表
                        jumpToList(webDriver, random, actions);
                        continue;
                    }

                    if(cbgItemService.insertByCode(cbgItem) == 1) {
                        insertNumbers ++;
                        System.out.println("insertNumbers : " + insertNumbers);
                    }else {
                        repeatNumbers ++;
                        System.out.println("repeatNumbers : " + repeatNumbers);
                    }


                    //跳转回列表
                        jumpToList(webDriver, random, actions);
                    }
            }catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                continue;
            }


        }
        System.out.println("insertNumbers : " + insertNumbers);
        System.out.println("repeatNumbers : " + repeatNumbers);
        ConstantUtils.resultSavePath = appConfigFromPost.getResultSavePath();

        webDriverPool.returnToPool(webDriver);
        return page;
    }

    private void doLoginV2(WebDriver webDriver, Random random) throws InterruptedException {
        Cookie cookie1 = new Cookie("sid", "v2.s.t0QQedS4YQ_etKXXwmm91KJdh4myKm90xyng1rv-eZ6p-YX8");
        // eWQuNDI0NThlOTBjYzE4NDFiNTlAMTYzLmNvbSRlOTFkNjBmZTg1MWExODlhMTA4YjMwZjBjOGYwZWQyOQ==
        Cookie cookie2 = new Cookie("urs_share_login_token_h5", "eWQuNDI0NThlOTBjYzE4NDFiNTlAMTYzLmNvbSRlOTFkNjBmZTg1MWExODlhMTA4YjMwZjBjOGYwZWQyOQ==");
        Cookie cookie3 = new Cookie("urs_share_login_token", "eWQuNDI0NThlOTBjYzE4NDFiNTlAMTYzLmNvbSRlOTFkNjBmZTg1MWExODlhMTA4YjMwZjBjOGYwZWQyOQ==");

        webDriver.manage().addCookie(cookie1);
        webDriver.manage().addCookie(cookie2);
        webDriver.manage().addCookie(cookie3);
        webDriver.manage().addCookie(new Cookie("is_log_active_stat","1"));
        webDriver.navigate().refresh();
        Thread.sleep(appConfigFromPost.getGetDetailUrlSleepTimeMillis() + random.nextInt(2000));

    }


    private void doLogin(WebDriver webDriver, Random random, Actions actions) throws InterruptedException {
        webDriver.switchTo().frame(webDriver.findElement(By.xpath("//iframe")).getAttribute("id"));
        WebElement mailModuleElement = webDriver.findElement(By.id("mailModule"));
        actions.click(mailModuleElement).build().perform();
        Thread.sleep(appConfigFromPost.getGetDetailUrlSleepTimeMillis() + random.nextInt(2000));

        //设置账号
        webDriver.findElement(By.className("dlemail")).sendKeys("gbk2024001@163.com");
        //设置密码
        webDriver.findElement(By.className("dlpwd")).sendKeys("Gbk19940411");

        //点击提交
        WebElement loginElement = webDriver.findElement(By.id("dologin"));
        actions.click(loginElement).build().perform();
        Thread.sleep(appConfigFromPost.getGetDetailUrlSleepTimeMillis() + random.nextInt(2000));
        // webDriver.switchTo().defaultContent();
        Thread.sleep(appConfigFromPost.getGetDetailUrlSleepTimeMillis() + random.nextInt(2000));
//                    break;
    }

    //跳转回列表
    private void jumpToList(WebDriver webDriver, Random random, Actions actions) throws InterruptedException {
        WebElement btnBackElement = webDriver.findElement(By.className("iff-icon-back"));
        actions.click(btnBackElement).build().perform();

        Thread.sleep(appConfigFromPost.getGetDetailUrlSleepTimeMillis() + random.nextInt(2000));
    }

    private void checkInit() {
        if (webDriverPool == null) {
            synchronized (this) {
                webDriverPool = new MyWebDriverPool(poolSize);
            }
        }
        if(cbgItemService == null) {

        }
    }

    public void setThread(int thread) {
        this.poolSize = thread;
    }

    public void close() throws IOException {
        webDriverPool.closeAll();
    }
}

