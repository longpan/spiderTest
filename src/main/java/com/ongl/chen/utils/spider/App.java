package com.ongl.chen.utils.spider;

import com.ongl.chen.utils.spider.processor.JDProductProcessor;
import org.springframework.context.ConfigurableApplicationContext;
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
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/spring/spring*.xml")) {
            final JDProductProcessor jdSpider = applicationContext.getBean(JDProductProcessor.class);

//        jobCrawler.crawl();
            jdSpider.start("奶粉");
        }
       
    }
}
