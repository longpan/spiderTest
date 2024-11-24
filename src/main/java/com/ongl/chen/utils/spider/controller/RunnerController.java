package com.ongl.chen.utils.spider.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ongl.chen.utils.spider.beans.CbgItem;
import com.ongl.chen.utils.spider.dao.CbgItemDAO;
import com.ongl.chen.utils.spider.pipline.CbgItemExcelPipline;
import com.ongl.chen.utils.spider.processor.*;
import com.ongl.chen.utils.spider.service.CbgItemService;
import com.ongl.chen.utils.spider.utils.AppConfigFromPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RunnerController {

    @Autowired
    private JDProductProcessor jdProductProcessor;

    @Autowired
    private GXRCWProcessor gxrcwProcessor;

    @Autowired
    private CbgMhxysyProcessorV5 cbgMhxysyProcessorV5;

    @Autowired
    private CbgItemDAO cbgItemDAO;

    @Autowired
    private CbgItemService cbgItemService;


    @GetMapping("/gxrcw")
    public void gxrcw(@RequestParam(required = true) String keyWord) {

        gxrcwProcessor.start(keyWord);
    }

    @GetMapping("/jd")
    public void jd(@RequestParam(required = true) String keyWord) {

        jdProductProcessor.start(keyWord);
    }

    @PostMapping("/cgb")
    public void cbg(@RequestBody AppConfigFromPost appConfigFromPost) throws InterruptedException {
        int cycleIndex = 1;
        while (true) {
            try {
                System.out.println("cbgMhxysyProcessorV5.start times: " + cycleIndex);
                cbgMhxysyProcessorV5.start(appConfigFromPost, cbgItemService);
                Thread.sleep(60*1000*5);
            }catch (Exception e){
                System.out.println("RunnerController Exception, errMsg: " + e.getMessage());
            }


        }

    }

    @GetMapping("/test")
    public void test() {
        CbgItem cbgItem = new CbgItem();
        cbgItem.setCode("xxx");
//        cbgItemDAO.insert(cbgItem);
        cbgItemService.insertByCode(cbgItem);
    }

    @GetMapping("/exportCbg")
    public void exportCbg() {
        CbgItem cbgItem = new CbgItem();
        cbgItem.setCode("xxx");
        List<CbgItem> cbgItemList =   cbgItemDAO.selectList(new QueryWrapper<>());
        CbgItemExcelPipline.saveExcel(cbgItemList);
//        cbgItemDAO.insert(cbgItem);
       // cbgItemService.insertByCode(cbgItem);
    }
}
