package com.ongl.chen.utils.spider.downloader.cbg;

import com.ongl.chen.utils.spider.downloader.MyWebDriverPool;
import com.ongl.chen.utils.spider.utils.AppConfigFromPostForCbg;
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
import java.util.Random;

/**
 * Created by apple on 2025/12/15.
 */
public class CbgMhxySeleniuDownloader implements Downloader, Closeable {
    private volatile MyWebDriverPool webDriverPool;


    private int sleepTime = 3000;

    private int poolSize = 1;

    AppConfigFromPostForCbg appConfigFromPostForCbg;

    private static final String DRIVER_PHANTOMJS = "phantomjs";

    Random random = new Random();

    /**
     * 新建
     *
     * @param chromeDriverPath chromeDriverPath
     */
    public CbgMhxySeleniuDownloader(String chromeDriverPath, AppConfigFromPostForCbg appConfigFromPostForCbg) {
        System.getProperties().setProperty("webdriver.chrome.driver",
                chromeDriverPath);
        this.appConfigFromPostForCbg = appConfigFromPostForCbg;
        this.sleepTime = appConfigFromPostForCbg.getGetDetailUrlSleepTimeMillis();
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
        String loginMode = appConfigFromPostForCbg != null ? appConfigFromPostForCbg.getLoginMode() : null;
        if (loginMode == null || loginMode.trim().isEmpty()) {
            loginMode = "auto";
        } else {
            loginMode = loginMode.trim().toLowerCase();
        }

        boolean hasCookieAuth = appConfigFromPostForCbg != null && StringUtils.isNotBlank(appConfigFromPostForCbg.getSid());
        boolean useCookieAuth = "cookie".equals(loginMode) || ("auto".equals(loginMode) && hasCookieAuth);

        if ("cookie".equals(loginMode) && !hasCookieAuth) {
            throw new RuntimeException("loginMode=cookie but sid is blank");
        }

        if (useCookieAuth) {
            webDriver.get("https://xyq.cbg.163.com/");
            doLogin(webDriver, appConfigFromPostForCbg);
        }
        webDriver.get(request.getUrl());

        String currentUrl = webDriver.getCurrentUrl();
        String title = "";
        try {
            title = webDriver.getTitle();
        } catch (Exception e) {
        }
        System.out.println("currentUrl = " + currentUrl);
        if (StringUtils.contains(request.getUrl(), "cgi-bin/query.py") && (StringUtils.equals(currentUrl, "https://xyq.cbg.163.com/") || StringUtils.equals(currentUrl, "https://xyq.cbg.163.com"))) {
            System.out.println("redirect_to_home title = " + title);
            throw new RuntimeException("redirect_to_home");
        }
        if(!StringUtils.equals(request.getUrl(), currentUrl)) {
            if (useCookieAuth) {
                doLogin(webDriver, appConfigFromPostForCbg);
            }
            webDriver.navigate().refresh();
            webDriver.navigate().to(request.getUrl());
            currentUrl = webDriver.getCurrentUrl();
            try {
                title = webDriver.getTitle();
            } catch (Exception e) {
            }
            System.out.println("currentUrl_after_login = " + currentUrl);
            if (StringUtils.contains(request.getUrl(), "cgi-bin/query.py") && (StringUtils.equals(currentUrl, "https://xyq.cbg.163.com/") || StringUtils.equals(currentUrl, "https://xyq.cbg.163.com"))) {
                System.out.println("redirect_to_home_after_login title = " + title);
                throw new RuntimeException("redirect_to_home_after_login");
            }
        }

        if(StringUtils.contains(currentUrl, "act=show_mbauth")
                || StringUtils.contains(currentUrl, "act=show_anon_auth_page")
                || StringUtils.contains(currentUrl, "/cgi-bin/login.py")) {
            System.out.println("当前页面需要认证，退出本次请求: " + currentUrl);
            throw new RuntimeException("需要认证: " + currentUrl);
        }

        try {
            Thread.sleep( (sleepTime + random.nextInt(3000)));
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

    private void doLogin(WebDriver webDriver, AppConfigFromPostForCbg appConfigFromPostForCbg) {
        //update
        Cookie cookie1 = new Cookie("login_id", appConfigFromPostForCbg.getLogin_id());
        Cookie cookie2 = new Cookie("login_user_roleid", "18922042");
        Cookie cookie3 = new Cookie("urs_share_login_token", "eWQuNDI0NThlOTBjYzE4NDFiNTlAMTYzLmNvbSRlOTFkNjBmZTg1MWExODlhMTA4YjMwZjBjOGYwZWQyOQ==");

        webDriver.manage().addCookie(cookie1);
        webDriver.manage().addCookie(cookie2);
        webDriver.manage().addCookie(cookie3);
        webDriver.manage().addCookie(new Cookie("is_log_active_stat","1"));
        webDriver.manage().addCookie(new Cookie("area_id", "9"));
        //update
        webDriver.manage().addCookie(new Cookie("cbg_qrcode", appConfigFromPostForCbg.getCbg_qrcode()));
        //update
        webDriver.manage().addCookie(new Cookie("reco_sid", appConfigFromPostForCbg.getReco_sid()));
        webDriver.manage().addCookie(new Cookie("login_user_urs", "lmj202412@163.com"));
        //update
        webDriver.manage().addCookie(new Cookie("sid", appConfigFromPostForCbg.getSid()));
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
