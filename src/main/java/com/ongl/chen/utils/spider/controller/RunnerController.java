package com.ongl.chen.utils.spider.controller;

import com.ongl.chen.utils.spider.processor.GXRCWProcessor;
import com.ongl.chen.utils.spider.processor.JDProductProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RunnerController {

    @Autowired
    private JDProductProcessor jdProductProcessor;

    @Autowired
    private GXRCWProcessor gxrcwProcessor;

    @GetMapping("/gxrcw")
    public void gxrcw(@RequestParam(required = true) String keyWord) {

        gxrcwProcessor.start(keyWord);
    }

    @GetMapping("/jd")
    public void jd(@RequestParam(required = true) String keyWord) {

        jdProductProcessor.start(keyWord);
    }
}
