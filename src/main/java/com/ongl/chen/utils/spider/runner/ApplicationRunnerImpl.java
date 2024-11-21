package com.ongl.chen.utils.spider.runner;

import com.ongl.chen.utils.spider.processor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author lnj
 * createTime 2018-11-07 22:37
 **/
@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JDProductProcessor jdProductProcessor;

    @Autowired
    private GXRCWProcessor gxrcwProcessor;

    @Autowired
    private CbgMhxysyProcessorV2 cbgMhxysyProcessorV2;

    @Autowired
    private CbgMhxysyProcessorV3 cbgMhxysyProcessorV3;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("通过实现ApplicationRunner接口，在spring boot项目启动后打印参数");
        String[] sourceArgs = args.getSourceArgs();
        for (String arg : sourceArgs) {
            System.out.print(arg + " ");
        }
    //    gxrcwProcessor.start("java");
//       cbgMhxysyProcessorV2.start();

    }
}