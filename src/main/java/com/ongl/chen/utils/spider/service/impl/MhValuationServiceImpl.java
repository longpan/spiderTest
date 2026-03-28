package com.ongl.chen.utils.spider.service.impl;

import com.ongl.chen.utils.spider.beans.dbg.MhEquipItem;
import com.ongl.chen.utils.spider.beans.dbg.MhLingShiItem;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;
import com.ongl.chen.utils.spider.service.MhValuationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 梦幻西游物品 AI 估值服务实现类
 */
@Service
public class MhValuationServiceImpl implements MhValuationService {

    @Autowired(required = false)
    private RestTemplate restTemplate;

    private static final String AI_SERVICE_URL = "http://localhost:8000/predict";

    @Override
    public void valuatePet(MhPetItem item) {
        if (item == null) return;
        try {
            Map<String, Object> features = new HashMap<>();
            features.put("type", "pet");
            features.put("level", parseDouble(item.getLevel()));
            features.put("skillNum", item.getSkillNum());
            features.put("attackQualification", parseDouble(item.getAttackQualification()));
            features.put("defenseQualification", parseDouble(item.getDefenseQualification()));
            features.put("physicalQualification", parseDouble(item.getPhysicalQualification()));
            features.put("manaQualification", parseDouble(item.getManaQualification()));
            features.put("speedQualification", parseDouble(item.getSpeedQualification()));
            features.put("growUp", parseDouble(item.getGrowUp()));
            features.put("collect", parseDouble(item.getCollect()));

            Double estimatedValue = callExternalAIService(features);
            double price = parseDouble(item.getPrice());
            
            if (estimatedValue == null) {
                estimatedValue = price * 1.0; // Fallback
            }

            item.setValuationValue(Math.round(estimatedValue * 100.0) / 100.0);
            double score = (price > 0) ? (estimatedValue / price) : 0.0;
            item.setValuationScore(Math.round(score * 100.0) / 100.0);
            updateLabel(item, score);
        } catch (Exception e) {
            System.err.println("Pet valuation failed: " + e.getMessage());
        }
    }

    @Override
    public void valuateEquip(MhEquipItem item) {
        if (item == null) return;
        try {
            Map<String, Object> features = new HashMap<>();
            features.put("type", "equip");
            features.put("level", parseDouble(item.getLevel()));
            features.put("accessoryAttributeNum", item.getAccessoryAttributeNum());
            features.put("collect", parseDouble(item.getCollect()));
            // 简单特征提取：主属性数值
            features.put("primeValue", parseDouble(item.getPrimeAttribute()));

            Double estimatedValue = callExternalAIService(features);
            double price = parseDouble(item.getPrice());

            if (estimatedValue == null) {
                estimatedValue = price * 1.0;
            }

            item.setValuationValue(Math.round(estimatedValue * 100.0) / 100.0);
            double score = (price > 0) ? (estimatedValue / price) : 0.0;
            item.setValuationScore(Math.round(score * 100.0) / 100.0);
            updateLabel(item, score);
        } catch (Exception e) {
            System.err.println("Equip valuation failed: " + e.getMessage());
        }
    }

    @Override
    public void valuateLingShi(MhLingShiItem item) {
        if (item == null) return;
        try {
            Map<String, Object> features = new HashMap<>();
            features.put("type", "lingshi");
            features.put("level", parseDouble(item.getLevel()));
            features.put("accessoryAttributeNum", item.getAccessoryAttributeNum());
            features.put("collect", parseDouble(item.getCollect()));
            features.put("primeValue", parseDouble(item.getPrimeAttribute()));

            Double estimatedValue = callExternalAIService(features);
            double price = parseDouble(item.getPrice());

            if (estimatedValue == null) {
                estimatedValue = price * 1.0;
            }

            item.setValuationValue(Math.round(estimatedValue * 100.0) / 100.0);
            double score = (price > 0) ? (estimatedValue / price) : 0.0;
            item.setValuationScore(Math.round(score * 100.0) / 100.0);
            updateLabel(item, score);
        } catch (Exception e) {
            System.err.println("LingShi valuation failed: " + e.getMessage());
        }
    }

    private Double callExternalAIService(Map<String, Object> features) {
        if (restTemplate == null) return null;
        try {
            Map<String, Object> response = restTemplate.postForObject(AI_SERVICE_URL, features, Map.class);
            if (response != null && response.containsKey("estimated_price")) {
                return Double.valueOf(response.get("estimated_price").toString());
            }
        } catch (Exception e) {
            System.out.println("AI Service unavailable, using fallback...");
        }
        return null;
    }

    private void updateLabel(Object item, double score) {
        String label;
        if (score >= 1.5) label = "AI 深度发现: 极品捡漏! (" + String.format("%.2f", score) + ")";
        else if (score >= 1.2) label = "AI 推荐: 性价比极高 (" + String.format("%.2f", score) + ")";
        else if (score >= 1.0) label = "AI 评估: 价格合理 (" + String.format("%.2f", score) + ")";
        else if (score >= 0.8) label = "AI 提醒: 价格略高 (" + String.format("%.2f", score) + ")";
        else label = "AI 警告: 割韭菜价格 (" + String.format("%.2f", score) + ")";

        try {
            item.getClass().getMethod("setTxt", String.class).invoke(item, label);
        } catch (Exception ignored) {}
    }

    private double parseDouble(String value) {
        if (StringUtils.isBlank(value)) return 0.0;
        try {
            String cleanValue = value.replaceAll("[^0-9.]", "");
            if (StringUtils.isBlank(cleanValue)) return 0.0;
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
