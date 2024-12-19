package com.ongl.chen.utils.spider.downloader.cbg;

import com.ongl.chen.utils.spider.downloader.MyWebDriverPool;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * Created by apple on 2025/12/15.
 */
public class CbgMhxySeleniuDownloader implements Downloader, Closeable {
    private volatile MyWebDriverPool webDriverPool;


    private int sleepTime = 3000;

    private int poolSize = 1;

    private static final String DRIVER_PHANTOMJS = "phantomjs";

    /**
     * 新建
     *
     * @param chromeDriverPath chromeDriverPath
     */
    public CbgMhxySeleniuDownloader(String chromeDriverPath) {
        System.getProperties().setProperty("webdriver.chrome.driver",
                chromeDriverPath);
    }

    /**
     * Constructor without any filed. Construct PhantomJS browser
     *
     * @author bob.li.0718@gmail.com
     */
    public CbgMhxySeleniuDownloader() {
        // System.setProperty("phantomjs.binary.path",
        // "/Users/Bingo/Downloads/phantomjs-1.9.7-macosx/bin/phantomjs");
    }

    /**
     * set sleep time to wait until load success
     *
     * @param sleepTime sleepTime
     * @return this
     */
    public CbgMhxySeleniuDownloader setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    public Page download(Request request, Task task) {
        checkInit();
        WebDriver webDriver;
        try {
            webDriver = webDriverPool.get();
        } catch (InterruptedException e) {
            return null;
        }
        System.out.println("spider star .... startUrl = " + request.getUrl());
        webDriver.get(request.getUrl());

        String currentUrl = webDriver.getCurrentUrl();
        System.out.println("currentUrl = " + currentUrl);
        if(!StringUtils.equals(request.getUrl(), currentUrl)) {
            doLogin(webDriver);
            webDriver.navigate().refresh();
            webDriver.navigate().to(request.getUrl());
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
        webDriverPool.returnToPool(webDriver);
        return page;
    }

    private void doLogin(WebDriver webDriver) {
        //update
        Cookie cookie1 = new Cookie("login_id", "c01280d7-bd5c-11ef-bb77-67111e799003");
        Cookie cookie2 = new Cookie("login_user_roleid", "18922042");
        Cookie cookie3 = new Cookie("urs_share_login_token", "eWQuNDI0NThlOTBjYzE4NDFiNTlAMTYzLmNvbSRlOTFkNjBmZTg1MWExODlhMTA4YjMwZjBjOGYwZWQyOQ==");

        webDriver.manage().addCookie(cookie1);
        webDriver.manage().addCookie(cookie2);
        webDriver.manage().addCookie(cookie3);
        webDriver.manage().addCookie(new Cookie("is_log_active_stat","1"));
        webDriver.manage().addCookie(new Cookie("area_id", "9"));
        //update
        webDriver.manage().addCookie(new Cookie("cbg_qrcode", "v2.s.98SkE7tD30mZsVlu3u2PW6k0OpY08zqm4po6mv1wYoaWrDeS"));
        //update
        webDriver.manage().addCookie(new Cookie("reco_sid", "xMEQUOpgtFo6hHfsXl9oftqOBUti6_cOcv4Nsv9f"));
        webDriver.manage().addCookie(new Cookie("login_user_urs", "lmj202412@163.com"));
        //update
        webDriver.manage().addCookie(new Cookie("sid", "v2.s.Zq8C8M71bK-N87HwSiSwgQTtsP2p-ty4ARSyrjOfCuQQOk9I"));
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

