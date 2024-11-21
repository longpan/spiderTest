package com.ongl.chen.utils.spider.downloader;

import com.ongl.chen.utils.spider.utils.AppConfig;
import com.ongl.chen.utils.spider.utils.AppConfigFromPost;
import com.ongl.chen.utils.spider.utils.ConstantUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Created by apple on 2018/8/29.

 v3:关键参数读取配置，在linux上运行
 */

public class CbgSeleniuDownloaderV4 implements Downloader, Closeable {
    public volatile MyWebDriverPool webDriverPool;


    private int sleepTime = 2000;

    private int poolSize = 1;

    private static final String DRIVER_PHANTOMJS = "phantomjs";

    private AppConfigFromPost appConfigFromPost;


    /**
     * 新建
     *
     * @param chromeDriverPath chromeDriverPath
     */
    public CbgSeleniuDownloaderV4(String chromeDriverPath) {
        System.getProperties().setProperty("webdriver.chrome.driver",
                chromeDriverPath);
    }

    /**
     * 新建
     *
     * @param chromeDriverPath chromeDriverPath
     */
    public CbgSeleniuDownloaderV4(String chromeDriverPath, AppConfigFromPost appConfigFromPost) {
        System.getProperties().setProperty("webdriver.chrome.driver",
                chromeDriverPath);
        this.appConfigFromPost = appConfigFromPost;
    }

    /**
     * Constructor without any filed. Construct PhantomJS browser
     *
     * @author bob.li.0718@gmail.com
     */
    public CbgSeleniuDownloaderV4() {
        // System.setProperty("phantomjs.binary.path",
        // "/Users/Bingo/Downloads/phantomjs-1.9.7-macosx/bin/phantomjs");
    }

    /**
     * set sleep time to wait until load success
     *
     * @param sleepTime sleepTime
     * @return this
     */
    public CbgSeleniuDownloaderV4 setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    @SneakyThrows
    public Page download(Request request, Task task) {
        checkInit();
        WebDriver webDriver;
        try {
            webDriver = webDriverPool.get();
        } catch (InterruptedException e) {
            return null;
        }
        webDriver.get(request.getUrl());
                                                        //v2.s.2pT8N8wRrXYfWO_VIROZjt6GMGlzyzKQZLlh3uNpwDFBxQVg
        /*Cookie cookie1 = new Cookie("sid", "v2.s.J1jWox3y3NdLscr8gi9kqwPjHa25DsMbXIARb3ngQ4UmFc-w");
                                                                        // eWQuNDI0NThlOTBjYzE4NDFiNTlAMTYzLmNvbSRlOTFkNjBmZTg1MWExODlhMTA4YjMwZjBjOGYwZWQyOQ==
        Cookie cookie2 = new Cookie("urs_share_login_token_h5", "eWQuNDI0NThlOTBjYzE4NDFiNTlAMTYzLmNvbSRlOTFkNjBmZTg1MWExODlhMTA4YjMwZjBjOGYwZWQyOQ==");
        Cookie cookie3 = new Cookie("urs_share_login_token", "eWQuNDI0NThlOTBjYzE4NDFiNTlAMTYzLmNvbSRlOTFkNjBmZTg1MWExODlhMTA4YjMwZjBjOGYwZWQyOQ==");

        webDriver.manage().addCookie(cookie1);
        webDriver.manage().addCookie(cookie2);
        webDriver.manage().addCookie(cookie3);
        webDriver.manage().addCookie(new Cookie("is_log_active_stat","1"));
        webDriver.navigate().refresh();*/

            //将页面滚动条拖到底部
            int step = 13000;
            int start_y = 500;

            for (int index = 0; index < appConfigFromPost.getMaxPullDownCount(); index ++) {
                int postion_y = start_y + step * index;
                ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(0," + postion_y + ");");
//            ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(0,document.body.scrollHeight);");
                //actions.click().build().perform();
                try {
                    System.out.println(index);
                    Thread.sleep(appConfigFromPost.getPullDownSleepTimeMillis());
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


        Actions actions = new Actions(webDriver);

        WebElement btnCloseElement = webDriver.findElement(By.className("btn-close"));

        // 使用Actions类模拟鼠标点击
        actions.click(btnCloseElement).build().perform();

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
        for(WebElement itemElement : itemListElement) {

            try{
                actions.click(itemElement).build().perform();
                Thread.sleep(appConfigFromPost.getGetDetailUrlSleepTimeMillis());
                String detail = webDriver.getCurrentUrl();
                System.out.println(detail);
                System.out.println(index);
                index++;
                ConstantUtils.detaiUrlList.add(detail);

                if(StringUtils.contains(detail, "equip")) {
                    WebElement btnBackElement = webDriver.findElement(By.className("iff-icon-back"));
                    actions.click(btnBackElement).build().perform();

                    Thread.sleep(appConfigFromPost.getGetDetailUrlSleepTimeMillis());
                }
            }catch (Exception e) {
                System.out.println(e.getMessage());
                ConstantUtils.detaiUrlList.add("");
            }


        }

        webDriverPool.returnToPool(webDriver);
        return page;
    }

    private void checkInit() {
        if (webDriverPool == null) {
            synchronized (this) {
                webDriverPool = new MyWebDriverPool(poolSize);
            }
        }
    }

    public void setThread(int thread) {
        this.poolSize = thread;
    }

    public void close() throws IOException {
        webDriverPool.closeAll();
    }
}

