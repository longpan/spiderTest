package com.ongl.chen.utils.spider.processor.cbg;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;
import com.ongl.chen.utils.spider.beans.cbg.CbgSpiderTask;
import com.ongl.chen.utils.spider.dao.MhPetItemDAO;
import com.ongl.chen.utils.spider.service.CbgSpiderTaskService;
import com.ongl.chen.utils.spider.service.MhValuationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.*;

/**
 * 藏宝阁宠物详情页处理器
 */
@Component
public class CbgPetDetailPageProcessor implements PageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CbgPetDetailPageProcessor.class);

    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    @Autowired
    private MhPetItemDAO mhPetItemDAO;

    @Autowired
    private CbgSpiderTaskService cbgSpiderTaskService;

    @Autowired
    private MhValuationService mhValuationService;

    @Override
    public void process(Page page) {
        String pageUrl = page.getUrl().toString();
        
        if (!StringUtils.contains(pageUrl, "xyq.cbg.163.com/equip?")) {
            return;
        }

        logger.info("Processing pet detail page: {}", pageUrl);
        
        // 记录是否成功保存了宠物数据
        boolean dataSaved = false;

        try {
            MhPetItem item = new MhPetItem();
            item.setDetailUrl(pageUrl);

            // 解析大区和服务器
            String serverInfo = page.getHtml().xpath("//div[@class='userInfo']/p[1]/text()").toString();
            if (StringUtils.isNotBlank(serverInfo) && serverInfo.contains("->")) {
                String[] parts = serverInfo.split("->");
                if (parts.length >= 1) {
                    item.setArea(parts[0].trim());
                }
                if (parts.length >= 2) {
                    item.setServerName(parts[1].trim());
                }
            }

            // 解析名称和等级
            String nameStr = page.getHtml().$(".names").xpath("li/text()").toString();
            if (StringUtils.isNotBlank(nameStr)) {
                String[] parts = StringUtils.split(nameStr, " ");
                if (parts.length >= 1) {
                    item.setName(parts[0]);
                }
                if (parts.length >= 2) {
                    item.setLevel(parts[1]);
                }
            }

            // 解析编号
            String code = page.getHtml().regex("编号：</strong>\\s*([0-9]+)", 1).toString();
            
            // 如果解析不到编号，说明页面不是正常的详情页，可能重定向到了其他页面
            if (StringUtils.isBlank(code)) {
                logger.error("无法解析物品编号，可能页面异常或需要登录: {}", pageUrl);
                throw new RuntimeException("无法解析物品编号，页面可能异常: " + pageUrl);
            }
            
            item.setCode(code);

            // 解析技能数量和技能列表
            int skillNum = page.getHtml().$("#pet_skill_grid_con").xpath("//img").nodes().size();
            List<String> skillList = page.getHtml().$("#pet_skill_grid_con").xpath("//img/@data_store_name").all();
            item.setSkillNum(skillNum);
            if (skillList != null && !skillList.isEmpty()) {
                item.setSkillList(skillList.toString());
            }

            // 解析基础属性 - 气血、体质、魔法、法力等
            item.setBlood(page.getHtml().$(".tb02").xpath("//tr[1]/td[1]/text()").toString());
            item.setCorporeity(page.getHtml().$(".tb02").xpath("//tr[1]/td[2]/text()").toString());
            item.setMagic(page.getHtml().$(".tb02").xpath("//tr[2]/td[1]/text()").toString());
            item.setPower(page.getHtml().$(".tb02").xpath("//tr[2]/td[2]/text()").toString());
            item.setAttack(page.getHtml().$(".tb02").xpath("//tr[3]/td[1]/text()").toString());
            item.setStrength(page.getHtml().$(".tb02").xpath("//tr[3]/td[2]/text()").toString());
            item.setDefense(page.getHtml().$(".tb02").xpath("//tr[4]/td[1]/text()").toString());
            item.setEndurance(page.getHtml().$(".tb02").xpath("//tr[4]/td[2]/text()").toString());
            item.setSpeed(page.getHtml().$(".tb02").xpath("//tr[5]/td[1]/text()").toString());
            item.setAgility(page.getHtml().$(".tb02").xpath("//tr[5]/td[2]/text()").toString());
            item.setMageWound(page.getHtml().$(".tb02").xpath("//tr[6]/td[1]/text()").toString());
            item.setPotency(page.getHtml().$(".tb02").xpath("//tr[6]/td[2]/text()").toString());
            item.setMageDefence(page.getHtml().$(".tb02").xpath("//tr[7]/td[1]/text()").toString());

            // 解析资质 - 攻击资质、防御资质、成长等
            item.setAttackQualification(page.getHtml().$(".petZiZhiTb").xpath("//tr[1]/td[1]/span/text()").toString());
            item.setLifetime(page.getHtml().$(".petZiZhiTb").xpath("//tr[1]/td[2]/text()").toString());
            item.setDefenseQualification(page.getHtml().$(".petZiZhiTb").xpath("//tr[2]/td[1]/span/text()").toString());
            item.setGrowUp(page.getHtml().$(".petZiZhiTb").xpath("//tr[2]/td[2]/text()").toString());
            item.setPhysicalQualification(page.getHtml().$(".petZiZhiTb").xpath("//tr[3]/td[1]/span/text()").toString());
            item.setManaQualification(page.getHtml().$(".petZiZhiTb").xpath("//tr[4]/td[1]/span/text()").toString());
            item.setSpeedQualification(page.getHtml().$(".petZiZhiTb").xpath("//tr[5]/td[1]/span/text()").toString());
            item.setDodgeQualification(page.getHtml().$(".petZiZhiTb").xpath("//tr[6]/td[1]/span/text()").toString());
            item.setIsBaby(page.getHtml().$(".petZiZhiTb").xpath("//tr[7]/td[1]/span/text()").toString());

            // 从extraData获取列表页传递的数据
            String extraData = (String) page.getRequest().getExtra("extraData");
            if (StringUtils.isNotBlank(extraData)) {
                Map<String, Object> extras = JSON.parseObject(extraData, Map.class);
                item.setPrice((String) extras.get("price"));
                item.setCollect((String) extras.get("collect"));
                // 亮点信息
                if (extras.get("lightSpot1") != null) {
                    item.setLightSpot1((String) extras.get("lightSpot1"));
                }
                if (extras.get("lightSpot2") != null) {
                    item.setLightSpot2((String) extras.get("lightSpot2"));
                }
            }

            // 检查是否已存在该物品
            MhPetItem existItem = mhPetItemDAO.selectOne(new QueryWrapper<MhPetItem>().eq("code", code));
            Long itemId;
            
            if (existItem == null) {
                // 新增
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());
                mhPetItemDAO.insert(item);
                itemId = item.getId();
                logger.info("新增宠物数据: code={}, name={}", code, item.getName());
            } else {
                // 更新
                item.setId(existItem.getId());
                item.setCreateTime(existItem.getCreateTime());
                item.setUpdateTime(new Date());
                mhPetItemDAO.updateById(item);
                itemId = existItem.getId();
                logger.info("更新宠物数据: code={}, name={}", code, item.getName());
            }

            // 标记数据已成功保存
            dataSaved = true;

            // 调用估值服务
            if (mhValuationService != null) {
                try {
                    mhValuationService.valuatePet(item);
                } catch (Exception e) {
                    logger.warn("估值服务调用失败: {}", e.getMessage());
                }
            }

            // 完成任务并关联数据
            Long taskId = (Long) page.getRequest().getExtra("taskId");
            if (taskId != null) {
                cbgSpiderTaskService.completeTask(taskId, itemId, code);
            }

            logger.info("Completed detail page for item: code={}, name={}, price={}", code, item.getName(), item.getPrice());
            
        } catch (Exception e) {
            logger.error("详情页解析异常: url={}, error={}", pageUrl, e.getMessage());
            // 如果数据未成功保存，则标记任务为失败
            if (!dataSaved) {
                Long taskId = (Long) page.getRequest().getExtra("taskId");
                if (taskId != null) {
                    cbgSpiderTaskService.failTask(taskId, "详情页解析失败: " + e.getMessage());
                    logger.error("[CbgPetDetailPageProcessor] 详情页未成功保存宠物数据，已标记任务失败: taskId={}, url={}", taskId, pageUrl);
                }
            } else {
                // 数据已保存但后续步骤（如估值）失败，仍然标记任务完成
                throw e;
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}
