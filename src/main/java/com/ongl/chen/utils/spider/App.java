package com.ongl.chen.utils.spider;

import com.ongl.chen.utils.spider.beans.UserInfo;
import com.ongl.chen.utils.spider.hbasedao.UserInfoMapper;
import com.ongl.chen.utils.spider.processor.CsdnBlogDetailsRepoPageProcessor;
import com.ongl.chen.utils.spider.processor.JDProductProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        System.out.println( "Hello World!" );
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/spring/spring*.xml");
        final JDProductProcessor jdSpider = applicationContext.getBean(JDProductProcessor.class);

//        jobCrawler.crawl();
//        jdSpider.start();
        UserInfoMapper userInfoMapper = applicationContext.getBean(UserInfoMapper.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(3);
        userInfo.setName("Jerry222");
        userInfoMapper.addUser(userInfo);
    }
}
