package com.ongl.chen.utils.spider.controller;

import com.ongl.chen.utils.spider.processor.CbgMhxysyProcessorV3;
import com.ongl.chen.utils.spider.processor.CbgMhxysyProcessorV4;
import com.ongl.chen.utils.spider.processor.GXRCWProcessor;
import com.ongl.chen.utils.spider.processor.JDProductProcessor;
import com.ongl.chen.utils.spider.utils.AppConfigFromPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class RunnerController {

    @Autowired
    private JDProductProcessor jdProductProcessor;

    @Autowired
    private GXRCWProcessor gxrcwProcessor;

    @Autowired
    private CbgMhxysyProcessorV4 cbgMhxysyProcessorV4;


    @GetMapping("/gxrcw")
    public void gxrcw(@RequestParam(required = true) String keyWord) {

        gxrcwProcessor.start(keyWord);
    }

    @GetMapping("/jd")
    public void jd(@RequestParam(required = true) String keyWord) {

        jdProductProcessor.start(keyWord);
    }

    @PostMapping("/cgb")
    public void cbg(@RequestBody AppConfigFromPost appConfigFromPost) {

        cbgMhxysyProcessorV4.start(appConfigFromPost);
    }
}
