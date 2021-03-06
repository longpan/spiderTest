package com.ongl.chen.utils.spider.pipline;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.ongl.chen.utils.spider.beans.GXRFWJobDetail;
import com.ongl.chen.utils.spider.beans.JDProductDetail;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

@Component
public class GXRCWExcelPipline implements Pipeline {

    static String save_name_prefix = "gxrcw";

    public void process(ResultItems resultItems, Task task) {
        if(resultItems == null || resultItems.get("job_list") == null) {
            return;
        }
        List<JDProductDetail> productDetailList = resultItems.get("product_detail_list");
        String keyWord = resultItems.get("keyWord");
        saveExcel(productDetailList, keyWord);
    }

    public static void saveExcel(List<JDProductDetail> productDetailList, String keyWord) {
        // 写法2
        String basePath = System.getProperty("user.dir") + "/save-data/";
        basePath = "/Users/apple/projects/temp/saveData/";
        String fileName = basePath +  save_name_prefix + keyWord  + ".xlsx";
        // 这里 需要指定写用哪个class去写
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(fileName, GXRFWJobDetail.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("模板").build();
            excelWriter.write(productDetailList, writeSheet);
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }
}
