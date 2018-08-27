package com.ongl.chen.utils.spider.utils;

/**
 * Created by apple on 2018/8/27.
 */
public class FileUtil {

    public static String getIdByUrl(String url) {
        if(url == null || "".equals(url)) {
            return "";
        }else {
            return url.substring(url.lastIndexOf("/") + 1);
        }
    }
}
