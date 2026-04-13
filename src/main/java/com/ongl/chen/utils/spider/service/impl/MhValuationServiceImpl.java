package com.ongl.chen.utils.spider.service.impl;

import com.ongl.chen.utils.spider.beans.dbg.MhEquipItem;
import com.ongl.chen.utils.spider.beans.dbg.MhLingShiItem;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;
import com.ongl.chen.utils.spider.service.MhValuationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 梦幻西游物品 AI 估值服务实现类
 * 增强版: 新增技能权重、资质百分比、交叉特征等衍生特征计算
 */
@Service
public class MhValuationServiceImpl implements MhValuationService {

    @Autowired(required = false)
    private RestTemplate restTemplate;

    private static final String AI_SERVICE_URL = "http://localhost:8000/predict";

    // ==================== 稀有技能配置 ====================

    /** 稀有高价值技能集合 - 拥有这些技能的宠物价值显著提升 */
    private static final Set<String> RARE_SKILLS = new HashSet<>(Arrays.asList(
        "须弥真言", "力劈华山", "壁垒击破", "谛听", "出其不意",
        "观照万象", "死亡召唤", "林中鸟", "鬼魂", "夜战"
    ));

    /** 技能价值权重映射 - 特殊技能加分 */
    private static final Map<String, Integer> SKILL_WEIGHT_MAP;

    static {
        Map<String, Integer> map = new HashMap<>();
        // S级技能 - 极高价值加成
        map.put("须弥真言", 50);
        map.put("观照万象", 45);
        map.put("力劈华山", 40);
        map.put("谛听", 40);
        map.put("出其不意", 35);
        // A级技能 - 高价值加成
        map.put("壁垒击破", 25);
        map.put("死亡召唤", 25);
        map.put("林中鸟", 20);
        map.put("鬼魂", 15);
        map.put("夜战", 12);
        // B级技能 - 中等价值加成
        map.put("连击", 8);
        map.put("必杀", 8);
        map.put("偷袭", 6);
        map.put("吸血", 5);
        map.put("反击", 5);
        map.put("反震", 5);
        // C级技能 - 基础价值
        map.put("感知", 4);
        map.put("隐身", 4);
        map.put("敏捷", 3);
        map.put("飞行", 3);
        map.put("神佑复生", 10);
        SKILL_WEIGHT_MAP = Collections.unmodifiableMap(map);
    }

    /** 满资质参考值（用于计算资质百分比） */
    private static final double MAX_ATTACK_QUAL = 1600;     // 攻击资质上限参考值
    private static final double MAX_DEFENSE_QUAL = 1400;    // 防御资质上限参考值
    private static final double MAX_PHYSICAL_QUAL = 6500;   // 体力资质上限参考值
    private static final double MAX_MANA_QUAL = 2800;       // 法力资质上限参考值
    private static final double MAX_SPEED_QUAL = 1450;      // 速度资质上限参考值
    private static final double MAX_DODGE_QUAL = 1200;      // 闪避资质上限参考值

    // ==================== 估值缓存机制 ====================

    /** 估值缓存: key=特征指纹, value=缓存结果（含过期时间） */
    private static final Map<String, CachedValuation> valuationCache = new ConcurrentHashMap<>();

    /** 缓存默认TTL: 30分钟 (毫秒) */
    private static final long CACHE_TTL_MS = 30 * 60 * 1000L;

    /** 缓存最大容量，防止内存溢出 */
    private static final int MAX_CACHE_SIZE = 5000;

    /** 缓存命中计数器（用于监控） */
    private static final AtomicLong cacheHitCount = new AtomicLong(0);
    private static final AtomicLong cacheMissCount = new AtomicLong(0);

    /**
     * 缓存结果内部类
     */
    private static class CachedValuation {
        final double estimatedPrice;
        final long timestamp;
        final String source;  // "AI_Model", "Cache", "Local_Engine"

        CachedValuation(double estimatedPrice, String source) {
            this.estimatedPrice = estimatedPrice;
            this.timestamp = System.currentTimeMillis();
            this.source = source;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    @Override
    public void valuatePet(MhPetItem item) {
        if (item == null) return;
        try {
            Map<String, Object> features = new HashMap<>();
            // ========== 基础特征（原有） ==========
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

            // ========== 增强特征（新增） ==========
            double skillScore = calculateSkillScore(item.getSkillList(), item.getSkillNum());
            features.put("skillScore", Math.round(skillScore * 100.0) / 100.0);

            double qualPercent = calculateQualificationPercent(item);
            features.put("qualPercent", Math.round(qualPercent * 10000.0) / 10000.0);

            double growLevelScore = calculateGrowLevelScore(parseDouble(item.getGrowUp()), parseDouble(item.getLevel()));
            features.put("growLevelScore", Math.round(growLevelScore * 100.0) / 100.0);

            int rareSkillCount = countRareSkills(item.getSkillList());
            features.put("rareSkillCount", rareSkillCount);

            int isBabyFlag = "true".equalsIgnoreCase(StringUtils.trim(item.getIsBaby())) ||
                              "是".equals(item.getIsBaby()) || "1".equals(item.getIsBaby()) ? 1 : 0;
            features.put("isBaby", isBabyFlag);

            // ========== 三级估值策略（缓存 -> AI服务 -> 本地引擎）==========
            Double estimatedValue = resolveValuationWithCache(features, item, skillScore, qualPercent, growLevelScore, rareSkillCount);
            double price = parseDouble(item.getPrice());

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

    /**
     * 三级估值解析（带缓存）
     * 1. 查缓存 - 命中且未过期则直接返回
     * 2. 调AI服务 - 成功后写入缓存并返回
     * 3. 本地规则引擎 - AI不可达时使用，结果也写入缓存（短TTL）
     */
    private Double resolveValuationWithCache(Map<String, Object> features, MhPetItem item,
                                              double skillScore, double qualPercent,
                                              double growLevelScore, int rareSkillCount) {
        String cacheKey = buildCacheKey(features);

        // Level 1: 缓存命中
        CachedValuation cached = valuationCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            cacheHitCount.incrementAndGet();
            return cached.estimatedPrice;
        }
        // 清理过期条目
        if (cached != null && cached.isExpired()) {
            valuationCache.remove(cacheKey);
        }
        cacheMissCount.incrementAndGet();

        // Level 2: 调用AI服务
        Double aiResult = callExternalAIService(features);
        if (aiResult != null) {
            // AI结果缓存30分钟
            putCache(cacheKey, aiResult, "AI_Model");
            return aiResult;
        }

        // Level 3: 本地规则引擎（最终fallback）
        double localResult = calculateLocalFallback(item, skillScore, qualPercent, growLevelScore, rareSkillCount);
        // 本地引擎结果缓存10分钟（较短TTL，因为AI可能恢复）
        putCacheWithTTL(cacheKey, localResult, "Local_Engine", 10 * 60 * 1000L);
        return localResult;
    }

    /**
     * 构建缓存key: 使用核心特征的hash值作为唯一标识
     */
    private String buildCacheKey(Map<String, Object> features) {
        StringBuilder sb = new StringBuilder("pet:");
        // 使用影响估值的核心特征构建key
        String[] keyFields = {"type", "level", "skillNum", "skillScore", "attackQualification",
                              "speedQualification", "growUp", "qualPercent", "growLevelScore",
                              "rareSkillCount", "isBaby"};
        for (String field : keyFields) {
            Object val = features.get(field);
            sb.append(field).append("=").append(val != null ? val.toString() : "_").append("|");
        }
        return sb.toString();
    }

    /**
     * 写入缓存（默认TTL）
     */
    private void putCache(String key, double price, String source) {
        if (valuationCache.size() >= MAX_CACHE_SIZE) {
            cleanExpiredCache();
        }
        valuationCache.put(key, new CachedValuation(price, source));
    }

    /**
     * 写入缓存（自定义TTL，通过提前设置过期timestamp实现）
     */
    private void putCacheWithTTL(String key, double price, String source, long ttlMs) {
        if (valuationCache.size() >= MAX_CACHE_SIZE) {
            cleanExpiredCache();
        }
        CachedValuation cv = new CachedValuation(price, source);
        // 通过修改timestamp使isExpired()在ttlMs后返回true
        cv.timestamp = System.currentTimeMillis() + CACHE_TTL_MS - ttlMs;
        valuationCache.put(key, cv);
    }

    /**
     * 清理过期缓存条目
     */
    private void cleanExpiredCache() {
        Iterator<Map.Entry<String, CachedValuation>> it = valuationCache.entrySet().iterator();
        int removed = 0;
        while (it.hasNext()) {
            if (it.next().getValue().isExpired()) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            System.out.println("Cleaned " + removed + " expired cache entries. Current size: " + valuationCache.size());
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        long hits = cacheHitCount.get();
        long misses = cacheMissCount.get();
        long total = hits + misses;
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", valuationCache.size());
        stats.put("maxCacheSize", MAX_CACHE_SIZE);
        stats.put("hitCount", hits);
        stats.put("missCount", misses);
        stats.put("hitRate", total > 0 ? String.format("%.2f%%", (double) hits / total * 100) : "N/A");
        return stats;
    }

    /**
     * 手动清空缓存（供管理接口调用）
     */
    public void clearCache() {
        int size = valuationCache.size();
        valuationCache.clear();
        cacheHitCount.set(0);
        cacheMissCount.set(0);
        System.out.println("Valuation cache cleared. Removed " + size + " entries.");
    }

    private Double callExternalAIService(Map<String, Object> features) {
        if (restTemplate == null) return null;
        try {
            Map<String, Object> response = restTemplate.postForObject(AI_SERVICE_URL, features, Map.class);
            if (response != null && response.containsKey("estimated_price")) {
                return Double.valueOf(response.get("estimated_price").toString());
            }
        } catch (Exception e) {
            System.out.println("AI Service unavailable, trying fallback... (" + e.getMessage() + ")");
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

    // ==================== 特征工程计算方法 ====================

    /**
     * 计算技能价值评分
     * 解析skillList字符串，对每个技能按权重表加权计分
     *
     * @param skillList 技能列表字符串，格式为 "[连击, 必杀, 夜战, ...]"
     * @param skillNum  技能总数
     * @return 技能加权评分（归一化后）
     */
    private double calculateSkillScore(String skillList, int skillNum) {
        if (StringUtils.isBlank(skillList) || skillNum <= 0) return 0.0;

        double totalScore = 0.0;

        try {
            // skillList存储格式为 List.toString() -> "[连击, 必杀, ...]"
            // 解析出各个技能名称
            String cleaned = skillList.replace("[", "").replace("]", "");
            String[] skills = cleaned.split(",");

            for (String skill : skills) {
                String trimmedSkill = StringUtils.trim(skill);
                if (StringUtils.isNotBlank(trimmedSkill)) {
                    Integer weight = SKILL_WEIGHT_MAP.get(trimmedSkill);
                    if (weight != null) {
                        totalScore += weight;
                    } else {
                        // 未识别的普通技能，给1分基础分
                        totalScore += 1.0;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse skillList: " + e.getMessage());
        }

        // 归一化：将总分转换为相对于技能数量的评分，避免纯按技能数膨胀
        return (skillNum > 0) ? (totalScore / Math.sqrt(skillNum)) : 0.0;
    }

    /**
     * 计算综合资质百分比
     * 将各资质值除以对应满资质上限，求平均得到综合资质率
     *
     * @param item 宠物实体
     * @return 资质百分比 (0.0 ~ 1.0+)
     */
    private double calculateQualificationPercent(MhPetItem item) {
        double attackPct = parseDouble(item.getAttackQualification()) / MAX_ATTACK_QUAL;
        double defensePct = parseDouble(item.getDefenseQualification()) / MAX_DEFENSE_QUAL;
        double physicalPct = parseDouble(item.getPhysicalQualification()) / MAX_PHYSICAL_QUAL;
        double manaPct = parseDouble(item.getManaQualification()) / MAX_MANA_QUAL;
        double speedPct = parseDouble(item.getSpeedQualification()) / MAX_SPEED_QUAL;

        // 计算有值的资质项的平均百分比
        int count = 5;
        double sum = attackPct + defensePct + physicalPct + manaPct + speedPct;

        // 如果有闪避资质也纳入计算
        String dodgeQual = item.getDodgeQualification();
        if (StringUtils.isNotBlank(dodgeQual)) {
            sum += parseDouble(dodgeQual) / MAX_DODGE_QUAL;
            count++;
        }

        return sum / count;
    }

    /**
     * 计算成长等级交叉特征评分
     * 高成长 + 高等级 的宠物更值钱（成长x等级交叉特征）
     *
     * @param growUp 成长值
     * @param level  等级
     * @return 交叉特征得分
     */
    private double calculateGrowLevelScore(double growUp, double level) {
        // 成长*等级作为核心交叉特征，再乘以一个系数使其数值合理
        // 典型范围: growUp(0.8~1.3) * level(1~175) => 最大约227
        return growUp * level * 10.0; // 乘以10使数值更有区分度
    }

    /**
     * 统计稀有技能数量
     *
     * @param skillList 技能列表字符串
     * @return 稀有技能个数
     */
    private int countRareSkills(String skillList) {
        if (StringUtils.isBlank(skillList)) return 0;

        int count = 0;
        try {
            String cleaned = skillList.replace("[", "").replace("]", "");
            String[] skills = cleaned.split(",");

            for (String skill : skills) {
                String trimmedSkill = StringUtils.trim(skill);
                if (StringUtils.isNotBlank(trimmedSkill) && RARE_SKILLS.contains(trimmedSkill)) {
                    count++;
                }
            }
        } catch (Exception ignored) {}
        return count;
    }

    /**
     * 本地规则引擎 Fallback（增强版）
     * 当AI服务不可达时使用本地公式估值，基于增强特征
     *
     * 公式设计思路：
     * - 技能有基础价格加成，稀有技能额外加权
     * - 资质百分比影响基础价格倍率
     * - 成长x等级作为交叉特征加分
     * - 收藏人数反映市场热度
     *
     * @return 估算价格
     */
    private double calculateLocalFallback(MhPetItem item, double skillScore,
                                           double qualPercent, double growLevelScore,
                                           int rareSkillCount) {
        double attackQual = parseDouble(item.getAttackQualification());
        double growUp = parseDouble(item.getGrowUp());
        int skillNum = item.getSkillNum();
        double collect = parseDouble(item.getCollect());
        double price = parseDouble(item.getPrice());

        // ========== 基于游戏知识的增强估值公式 ==========

        // 1. 技能基础价值：技能数的平方体现非线性增长（多技能稀缺性）
        double skillBase = Math.pow(skillNum, 2) * 100;

        // 2. 稀有技能加成：每个稀有技能增加30%的基础技能价值
        double rareBonus = skillBase * rareSkillCount * 0.3;

        // 3. 技能评分加成：直接用解析出的skillScore进一步修正
        double scoreBonus = skillScore * 20;

        // 4. 攻击资质基础价（物理系宠物主要看攻击资质）
        double qualBase = attackQual * 0.8;

        // 5. 资质百分比加成：高资质宠物溢价
        // qualPercent > 0.85 表示接近满资，给予额外加成
        double qualBonus = 0.0;
        if (qualPercent > 0.90) {
            qualBonus = qualBase * 0.5;  // 满资级：50%溢价
        } else if (qualPercent > 0.80) {
            qualBonus = qualBase * 0.25; // 高资级：25%溢价
        } else if (qualPercent > 0.70) {
            qualBonus = qualBase * 0.1;  // 中上资级：10%溢价
        }

        // 6. 成长加成：成长越高越值钱
        double growBonus = growUp * 2000;

        // 7. 成长x等级交叉特征加分
        double crossFeatureBonus = growLevelScore * 50;

        // 8. 收藏热度系数（市场关注度）
        double collectBonus = collect * 3;

        // 9. 是否宝宝加成（宝宝比野生值钱得多）
        boolean isBaby = "true".equalsIgnoreCase(StringUtils.trim(item.getIsBaby())) ||
                         "是".equals(item.getIsBaby()) || "1".equals(item.getIsBaby());
        double babyMultiplier = isBaby ? 2.5 : 1.0; // 宝宝2.5倍加成

        // ========== 汇总计算 ==========
        double estimatedPrice = (skillBase + rareBonus + scoreBonus + qualBase + qualBonus
                                  + growBonus + crossFeatureBonus + collectBonus)
                                * babyMultiplier;

        // 保证最低返回原价（避免估值过低）
        return Math.max(estimatedPrice, price * 0.8);
    }
}
