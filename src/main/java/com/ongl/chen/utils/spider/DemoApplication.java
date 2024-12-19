package com.ongl.chen.utils.spider;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan({"com.gitee.sunchenbin.mybatis.actable.dao.*","com.ongl.chen.utils.spider.dao"})
@ComponentScan({"com.gitee.sunchenbin.mybatis.actable.manager.*","com.ongl.chen.utils.spider.*"})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

    }

}
