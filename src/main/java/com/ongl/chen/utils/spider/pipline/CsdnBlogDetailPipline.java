package com.ongl.chen.utils.spider.pipline;

import com.ongl.chen.utils.spider.beans.CsdnBlogDetail;
import com.ongl.chen.utils.spider.dao.BlogDetailDAO;
import org.springframework.beans.factory.annotation.Autowired;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * Created by apple on 2018/8/21.
 */
public class CsdnBlogDetailPipline implements Pipeline {

    @Autowired
    private BlogDetailDAO blogDetailDAO;

    public void process(ResultItems resultItems, Task task) {

        CsdnBlogDetail csdnBlogDetail = resultItems.get("blog");

        blogDetailDAO.insert(csdnBlogDetail);
    }
}



