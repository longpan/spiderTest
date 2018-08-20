package com.ongl.chen.utils.spider;

import com.ongl.chen.utils.spider.processor.CsdnBlogDetailsRepoPageProcessor;
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
        final CsdnBlogDetailsRepoPageProcessor csdnSpider = applicationContext.getBean(CsdnBlogDetailsRepoPageProcessor.class);
//        jobCrawler.crawl();
        csdnSpider.start();
    }
}
