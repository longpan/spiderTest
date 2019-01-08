package com.ongl.chen.utils.spider.pipline;

import com.ongl.chen.utils.spider.beans.CsdnBlogDetail;
import com.ongl.chen.utils.spider.beans.JDProductDetail;
import com.ongl.chen.utils.spider.dao.BlogDetailDAO;
import com.ongl.chen.utils.spider.dao.JDProductDetailDAO;
import com.ongl.chen.utils.spider.hbasedao.JDProductDetailHbaseDAO;
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

    @Autowired
    private JDProductDetailHbaseDAO jdProductDetailHbaseDAO;

    public static long id = 1;

    public void process(ResultItems resultItems, Task task) {

//        System.out.println("insert product");
        if(resultItems == null || resultItems.get("product_detail_list") == null) {
            return;
        }
        List<JDProductDetail> productDetailList = resultItems.get("product_detail_list");
        
        //todo： 改成批量插入
        for(JDProductDetail detail : productDetailList) {
            if(detail.getPId().equals("")){
                break;
            }
//            System.out.println(detail.toString());
            detail.setId(id++);
//            jdProductDetailDAO.insert(detail);
            jdProductDetailHbaseDAO.addProduct(detail);
        }
        
        



    }
}



