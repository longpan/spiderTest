package com.ongl.chen.utils.spider.pipline;

import com.ongl.chen.utils.spider.beans.CsdnBlogDetail;
import com.ongl.chen.utils.spider.beans.JDProductDetail;
import com.ongl.chen.utils.spider.dao.BlogDetailDAO;
import com.ongl.chen.utils.spider.dao.JDProductDetailDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

/**
 * Created by apple on 2018/8/21.
 */
@Component
public class JDProductDetailPipline implements Pipeline {

    @Autowired
    private JDProductDetailDAO jdProductDetailDAO;

    public void process(ResultItems resultItems, Task task) {

//        System.out.println("insert product");
        List<JDProductDetail> productDetailList = resultItems.get("product_detail_list");

        for(JDProductDetail detail : productDetailList) {
//            System.out.println(detail.toString());
            jdProductDetailDAO.insert(detail);
        }



    }
}



