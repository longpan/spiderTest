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

    public static int parseCommitNum(String commitStr) {
        if(commitStr == null || "".equals(commitStr) || commitStr.length() <= 1) {
            return 0;
        }else {
            String preStr ;
            String unit = commitStr.charAt(commitStr.length() -2) + "";
            boolean unitFlag = "万".equals(unit);
            if(unitFlag) {
                preStr = commitStr.substring(0, commitStr.length() -2);
            }else {
                preStr = commitStr.substring(0, commitStr.length() -1);
            }
            try {
                Double preNum = Double.parseDouble(preStr);
                if(unitFlag) {
                    Double result = preNum * 10000;
                    return result.intValue();
                }else  {
                    return preNum.intValue();
                }
            }catch (Exception e) {
                System.out.println("parse commit num err, " + commitStr);
                return 0;
            }

//            if("万".equals(unit)) {
//                try {
//                    int preNum = Integer.parseInt(preStr);
//                    return preNum * 10000;
//
//            }catch (Exception e) {
//                    System.out.println("parse commit num err, " + commitStr);
//                }finally {
//                    return 0;
//                }
//                }else {
//                preStr = commitStr.substring(0, commitStr.length() -1);
//
//            }
        }
    }
    public static void main( String[] args )
    {
        String commitStr = "3+";
        int result = parseCommitNum(commitStr);
        System.out.println(result);
    }

}
