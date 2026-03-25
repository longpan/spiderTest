package com.ongl.chen.utils.spider.service.impl;

import com.ongl.chen.utils.spider.beans.CbgItem;
import com.ongl.chen.utils.spider.service.CbgValuationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 数据价值挖掘服务实现 - XGBoost 模型集成 V3.0
 * 结合本地规则和远程 AI 模型服务 (FastAPI) 进行精准估值
 */
@Service
public class CbgValuationServiceImpl implements CbgValuationService {

    @Autowired(required = false)
    private RestTemplate restTemplate;

    private static final String AI_SERVICE_URL = "http://localhost:8000/predict";

    // ... (保留之前的权重 Map 作为备用)
    private static final Map<String, Double> SECT_WEIGHTS = new HashMap<String, Double>() {{
        put("大唐官府", 1.1);
        put("龙宫", 1.2);
        put("普陀山", 1.05);
        put("方寸山", 1.15);
        put("狮驼岭", 1.1);
        put("魔王寨", 1.1);
        put("阴曹地府", 1.05);
        put("化生寺", 1.15);
        put("女儿村", 1.2);
    }};

    private static final Map<Integer, Double> LEVEL_CORRECTION = new HashMap<Integer, Double>() {{
        put(69, 1.2); put(89, 1.0); put(109, 1.3); put(119, 1.1); put(175, 1.5);
    }};

    @Override
    public void valuate(CbgItem item) {
        if (item == null) return;

        try {
            // 1. 特征清洗与提取
            double price = parseDouble(item.getPrice());
            double overallScore = parseDouble(item.getOverallScore());
            double personScore = parseDouble(item.getPersonScore());
            int level = (int) parseDouble(item.getLevel());
            int collectCount = (int) parseDouble(item.getCollect());
            String sect = item.getWrapName();

            // 2. 调用远程 AI 服务 (XGBoost/LightGBM)
            Double estimatedValue = callExternalAIService(overallScore, personScore, level, sect, collectCount);

            // 3. 如果 AI 服务不可用，降级到本地规则模型 (Fallback)
            if (estimatedValue == null) {
                estimatedValue = calculateLocalRuleValue(overallScore, personScore, level, sect, collectCount, item.getBargin());
            }

            // 4. 计算性价比并更新
            double valuationScore = (price > 0) ? (estimatedValue / price) : 0.0;
            item.setValuationValue(Math.round(estimatedValue * 100.0) / 100.0);
            item.setValuationScore(Math.round(valuationScore * 100.0) / 100.0);

            // 5. 智能标注
            updateValuationLabel(item, valuationScore);

        } catch (Exception e) {
            System.err.println("AI 估值失败: " + e.getMessage());
        }
    }

    /**
     * 调用远程 Python AI 模型服务 (FastAPI + XGBoost)
     */
    private Double callExternalAIService(double score, double person, int lv, String sect, int collect) {
        if (restTemplate == null) return null;

        try {
            // 构造模型特征输入 (Features)
            Map<String, Object> request = new HashMap<>();
            request.put("overall_score", score);
            request.put("person_score", person);
            request.put("level", lv);
            request.put("sect", sect);
            request.put("collect_count", collect);

            // 发送 POST 请求到 Python 端
            Map<String, Object> response = restTemplate.postForObject(AI_SERVICE_URL, request, Map.class);

            if (response != null && response.containsKey("estimated_price")) {
                return Double.valueOf(response.get("estimated_price").toString());
            }
        } catch (Exception e) {
            System.out.println("AI 远程服务暂不可用，切换到本地规则模型...");
        }
        return null;
    }

    /**
     * 本地规则模型 (作为 AI 服务的降级方案)
     */
    private double calculateLocalRuleValue(double score, double person, int lv, String sect, int collect, String bargin) {
        double base = (score * 1.2) + (person * 1.8);
        double sectW = SECT_WEIGHTS.getOrDefault(sect, 1.0);
        double lvW = getLevelWeight(lv);
        double heat = calculateHeatFactor(collect);
        double barginF = StringUtils.isNotBlank(bargin) ? 0.95 : 1.0;
        return base * sectW * lvW * heat * barginF;
    }

    private double getLevelWeight(int level) {
        for (Map.Entry<Integer, Double> entry : LEVEL_CORRECTION.entrySet()) {
            if (level <= entry.getKey()) return entry.getValue();
        }
        return 1.0;
    }

    private double calculateHeatFactor(int collectCount) {
        if (collectCount <= 0) return 0.9;
        if (collectCount < 10) return 1.0;
        if (collectCount < 50) return 1.05;
        if (collectCount < 100) return 1.1;
        return 1.2;
    }

    private void updateValuationLabel(CbgItem item, double score) {
        if (score >= 1.5) item.setTxt("AI 深度挖掘: 极品大漏! (" + String.format("%.2f", score) + ")");
        else if (score >= 1.2) item.setTxt("AI 推荐: 性价比极高 (" + String.format("%.2f", score) + ")");
        else if (score >= 1.0) item.setTxt("AI 评估: 价格合理 (" + String.format("%.2f", score) + ")");
        else if (score >= 0.8) item.setTxt("AI 提醒: 价格略高 (" + String.format("%.2f", score) + ")");
        else item.setTxt("AI 警告: 割韭菜价格 (" + String.format("%.2f", score) + ")");
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
