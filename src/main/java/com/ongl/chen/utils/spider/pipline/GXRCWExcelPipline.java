package com.ongl.chen.utils.spider.pipline;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.ongl.chen.utils.spider.GXRFWJobDetail;
import com.ongl.chen.utils.spider.beans.JDProductDetail;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

@Component
public class GXRCWExcelPipline implements Pipeline {


    public void process(ResultItems resultItems, Task task) {
        if(resultItems == null || resultItems.get("job_list") == null) {
            return;
        }
        List<GXRFWJobDetail> jobList = resultItems.get("job_list");
        String keyWord = resultItems.get("keyWord");
        saveExcel(jobList, keyWord);
    }

    public static void saveExcel(List<GXRFWJobDetail> jobList, String keyWord) {
        // 写法2
        String basePath = System.getProperty("user.dir") + "/save-data/";
        basePath = "/Users/apple/projects/temp/saveData/";
        String fileName = basePath +  "gxrcw_" + keyWord  + ".xlsx";
        // 这里 需要指定写用哪个class去写
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(fileName, GXRFWJobDetail.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("模板").build();
            excelWriter.write(jobList, writeSheet);
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }
}
