package com.ongl.chen.utils.spider.processor.cbg;

import com.alibaba.fastjson.JSON;
import com.ongl.chen.utils.spider.beans.cbg.CbgSpiderTask;
import com.ongl.chen.utils.spider.service.CbgSpiderTaskService;
import com.ongl.chen.utils.spider.utils.AppConfigFromPostForCbg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.*;

/**
 * 藏宝阁宠物列表页处理器
 */
@Component
public class CbgPetListPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    @Autowired
    private CbgSpiderTaskService cbgSpiderTaskService;

    private static final String URL_INDEX = "https://xyq.cbg.163.com/cgi-bin/query.py?act=search_pet";

    @Override
    public void process(Page page) {
        String pageUrl = page.getUrl().toString();
        
        if (!StringUtils.contains(pageUrl, URL_INDEX)) {
            return;
        }

        System.out.println("Processing pet list page: " + pageUrl);
        
        // 解析列表页
        List<Selectable> petList = page.getHtml().$("#soldList").xpath("tr").nodes();
        
        // 记录创建的详情任务数量
        int createdDetailCount = 0;

        for (Selectable petSelectable : petList) {
            List<String> links = petSelectable.links().all();
            if (links == null || links.isEmpty()) {
                continue;
            }

            String detailUrl = StringUtils.strip(StringUtils.trim(links.get(0)), "` ");
            if (StringUtils.isBlank(detailUrl)) {
                continue;
            }

            // 提取列表页信息
            String price = petSelectable.xpath("//td[5]/span/text()").toString();
            
            // 提取亮点1
            List<Selectable> lightSpot1Selectable = petSelectable.xpath("//td[3]/a").nodes();
            StringBuilder lightSpot1Temp = new StringBuilder();
            for (int index = 1; index <= lightSpot1Selectable.size(); index++) {
                lightSpot1Temp.append(petSelectable.xpath("//td[3]/a[" + index + "]/text()").toString()).append(",");
            }

            // 提取亮点2
            List<Selectable> lightSpot2Selectable = petSelectable.xpath("//td[4]/span").nodes();
            StringBuilder lightSpot2Temp = new StringBuilder();
            for (int index = 1; index <= lightSpot2Selectable.size(); index++) {
                lightSpot2Temp.append(petSelectable.xpath("//td[4]/span[" + index + "]/text()").toString()).append(",");
            }
            
            String collect = petSelectable.xpath("//td[8]/div/text()").toString();

            // 构建额外数据
            Map<String, Object> extras = new HashMap<>();
            extras.put("price", price);
            extras.put("collect", collect);
            extras.put("lightSpot1", lightSpot1Temp.toString());
            extras.put("lightSpot2", lightSpot2Temp.toString());

            // 创建详情页子任务
            Long parentTaskId = (Long) page.getRequest().getExtra("taskId");
            cbgSpiderTaskService.createDetailPageTask(parentTaskId, detailUrl, extras);

            createdDetailCount++;
            System.out.println("Created detail task for: " + detailUrl);
        }
        
        // 如果当前页面无法提取到任何详情任务，则将列表任务标记为失败
        if (createdDetailCount == 0) {
            Long parentTaskId = (Long) page.getRequest().getExtra("taskId");
            if (parentTaskId != null) {
                cbgSpiderTaskService.failTask(parentTaskId, "列表页解析失败: 未提取到任何详情任务，可能页面结构变化或无数据");
                System.err.println("[CbgPetListPageProcessor] 列表页未提取到详情任务，已标记任务失败: taskId=" + parentTaskId + ", url=" + pageUrl);
            }
        } else {
            System.out.println("[CbgPetListPageProcessor] 列表页处理完成，共创建 " + createdDetailCount + " 个详情任务");
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}
