from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, Dict, Any
import uvicorn
import joblib
import pandas as pd
import os
from datetime import datetime

app = FastAPI(title="Mhxy Spider AI Valuation Service v2")

# ==================== 配置常量 ====================

# 1. 模型加载路径 (相对于脚本文件所在目录)
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_DIR = os.path.join(BASE_DIR, 'train_model')

# 2. 预加载模型 (如果文件不存在，将降级为增强规则逻辑)
models = {}
scalers_x = {}
scalers_y = {}
features_info = {}

try:
    # 尝试加载真实数据训练的模型
    models['pet'] = joblib.load(os.path.join(MODEL_DIR, 'mh_pet_model_real.pkl'))
    scalers_x['pet'] = joblib.load(os.path.join(MODEL_DIR, 'scaler_x_pet.pkl'))
    scalers_y['pet'] = joblib.load(os.path.join(MODEL_DIR, 'scaler_y_pet.pkl'))
    with open(os.path.join(MODEL_DIR, 'pet_features_info.json'), 'r', encoding='utf-8') as f:
        import json
        features_info['pet'] = json.load(f)
    print(f"[{datetime.now().strftime('%H:%M:%S')}] Real pet model loaded successfully from: {MODEL_DIR}")
except Exception as e1:
    try:
        # 如果真实模型不存在，尝试加载旧版模型
        models['pet'] = joblib.load(os.path.join(MODEL_DIR, 'mh_pet_model.pkl'))
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Legacy pet model loaded successfully from: {MODEL_DIR}")
    except Exception as e2:
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Warning: Failed to load pet models, using enhanced fallback logic. Error: {e2}")

# 尝试加载装备模型（如果存在）
try:
    models['equip'] = joblib.load(os.path.join(MODEL_DIR, 'mh_equip_model.pkl'))
    print(f"[{datetime.now().strftime('%H:%M:%S')}] Equipment model loaded successfully")
except:
    pass

# 尝试加载灵饰模型（如果存在）
try:
    models['lingshi'] = joblib.load(os.path.join(MODEL_DIR, 'mh_lingshi_model.pkl'))
    print(f"[{datetime.now().strftime('%H:%M:%S')}] Lingshi model loaded successfully")
except:
    pass

if models:
    print(f"[{datetime.now().strftime('%H:%M:%S')}] Total {len(models)} model(s) loaded")

# 3. 宠物类型配置（用于基于技能类型的精确估值）
# 不同宠物类型的技能价值权重不同
PET_TYPE_SKILL_WEIGHTS = {
    "物理系": {"attackQualification": 0.6, "speedQualification": 0.3},
    "法术系": {"manaQualification": 0.5, "speedQualification": 0.3},
    "血耐系": {"physicalQualification": 0.5, "defenseQualification": 0.4},
}

# 4. 稀有技能额外加成系数
RARE_SKILL_BONUS_MULTIPLIER = {
    # S级 - 极高价值
    "须弥真言": 1.8,
    "观照万象": 1.7,
    "力劈华山": 1.6,
    "谛听": 1.6,
    "出其不意": 1.5,
    # A级 - 高价值
    "壁垒击破": 1.35,
    "死亡召唤": 1.30,
    "林中鸟": 1.25,
    "鬼魂": 1.15,
    "夜战": 1.12,
}


# ==================== 数据模型定义 ====================

class ValuationRequest(BaseModel):
    """估值请求 - 增强版，支持Java端发送的衍生特征"""
    type: str  # 'pet', 'equip', 'lingshi'
    level: float
    collect: float
    # Pet 专属基础特征（原有）
    skillNum: Optional[int] = None
    attackQualification: Optional[float] = None
    defenseQualification: Optional[float] = None
    physicalQualification: Optional[float] = None
    manaQualification: Optional[float] = None
    speedQualification: Optional[float] = None
    growUp: Optional[float] = None
    # Equip/LingShi 专属特征（原有）
    accessoryAttributeNum: Optional[int] = None
    primeValue: Optional[float] = None
    # ====== 新增衍生特征（v2增强） ======
    skillScore: Optional[float] = None           # 技能价值评分（特殊技能加权）
    qualPercent: Optional[float] = None          # 综合资质百分比
    growLevelScore: Optional[float] = None       # 成长x等级交叉特征
    rareSkillCount: Optional[int] = None         # 稀有技能数量
    isBaby: Optional[int] = None                 # 是否宝宝(1=是/0=否)


# ==================== API 端点 ====================

@app.post("/predict")
async def predict(data: ValuationRequest):
    """
    接收 Java 端发送的特征，返回估值结果。
    
    处理流程：
    1. 尝试使用已训练的 sklearn 模型进行预测
    2. 模型不可用或失败时，使用增强版降级逻辑（基于游戏知识的公式）
    
    v2 增强:
    - 接收Java端计算的衍生特征（skillScore/qualPercent/growLevelScore等）
    - 降级公式使用更精确的游戏经济模型
    - 区分宠物类型进行差异化估值
    """
    try:
        model = models.get(data.type)

        # ========== 路径A: 使用训练好的AI模型预测 ==========
        if model:
            try:
                prediction_result = _predict_with_model(model, data, data.type)
                if prediction_result is not None:
                    return {
                        "estimated_price": round(prediction_result, 2),
                        "source": "AI_Model",
                        "features_used": list(_build_feature_dict(data).keys()),
                        "note": "Using real data trained model" if data.type in scalers_x else "Using legacy model"
                    }
            except Exception as model_err:
                print(f"[{datetime.now().strftime('%H:%M:%S')}] Model prediction failed: {model_err}, using fallback")

        # ========== 路径B: 增强版降级逻辑（基于游戏知识）==========
        estimated_price = _enhanced_fallback_logic(data)
        return {
            "estimated_price": round(estimated_price, 2),
            "source": "Enhanced_Fallback_v2",
            "note": "Using rule-based valuation with derived features"
        }

    except Exception as e:
        return {"error": str(e), "estimated_price": None}


@app.get("/health")
async def health_check():
    """健康检查端点"""
    loaded_models = [k for k, v in models.items() if v is not None]
    return {
        "status": "ok",
        "loaded_models": loaded_models,
        "version": "2.0"
    }


# ==================== 内部方法 ====================

def _build_feature_dict(data: ValuationRequest) -> Dict[str, Any]:
    """
    构建完整的特征字典，优先使用新增的衍生特征。
    如果Java端未发送新特征，则从基础字段计算。
    """
    features = {}

    # 基础特征（始终包含）
    features['type'] = data.type
    features['level'] = data.level or 0
    features['collect'] = data.collect or 0

    if data.type == "pet":
        features['skillNum'] = data.skillNum or 0
        features['attackQualification'] = data.attackQualification or 0
        features['defenseQualification'] = data.defenseQualification or 0
        features['physicalQualification'] = data.physicalQualification or 0
        features['manaQualification'] = data.manaQualification or 0
        features['speedQualification'] = data.speedQualification or 0
        features['growUp'] = data.growUp or 0

        # ===== 衍征特征（v2新增）=====
        # 如果Java端已计算好则直接使用，否则用Python本地简单计算作为兜底
        if data.skillScore is not None:
            features['skillScore'] = data.skillScore
        else:
            # 简单fallback: 仅用skillNum做粗略估算
            features['skillScore'] = max(0, (data.skillNum or 0) ** 1.5 * 5)

        if data.qualPercent is not None:
            features['qualPercent'] = data.qualPercent
        else:
            # 简单fallback: 用攻击资质近似
            attack_max = 1600.0
            features['qualPercent'] = min(1.5, ((data.attackQualification or 0) / attack_max))

        if data.growLevelScore is not None:
            features['growLevelScore'] = data.growLevelScore
        else:
            features['growLevelScore'] = ((data.growUp or 0) * (data.level or 0)) * 10

        if data.rareSkillCount is not None:
            features['rareSkillCount'] = data.rareSkillCount
        else:
            features['rareSkillCount'] = 0

        if data.isBaby is not None:
            features['isBaby'] = data.isBaby
        else:
            features['isBaby'] = 1  # 默认假设为宝宝

    elif data.type in ("equip", "lingshi"):
        features['accessoryAttributeNum'] = data.accessoryAttributeNum or 0
        features['primeValue'] = data.primeValue or 0

    return features


def _predict_with_model(model, data: ValuationRequest, model_type: str) -> Optional[float]:
    """
    使用sklearn模型进行预测。
    支持真实数据训练的模型（含标准化处理）。
    """
    feat_dict = _build_feature_dict(data)
    
    if model_type == "pet":
        # 创建特征DataFrame
        df = pd.DataFrame([feat_dict])
        
        # 检查是否是真实数据训练的模型（带有标准化器）
        if model_type in scalers_x and model_type in scalers_y:
            # 使用真实数据训练的模型（带标准化处理）
            try:
                # 获取特征顺序
                if model_type in features_info:
                    feature_order = features_info[model_type]['feature_columns']
                else:
                    feature_order = list(feat_dict.keys())
                
                # 按正确顺序提取特征
                X = pd.DataFrame([feat_dict])[feature_order]
                
                # 标准化特征
                X_scaled = scalers_x[model_type].transform(X)
                
                # 预测
                y_pred_scaled = model.predict(X_scaled)
                
                # 反标准化目标值
                y_pred = scalers_y[model_type].inverse_transform(y_pred_scaled.reshape(-1, 1))
                
                return float(y_pred[0][0])
            except Exception as e:
                print(f"[{datetime.now().strftime('%H:%M:%S')}] Real model prediction error: {e}, falling back to direct prediction")
        
        # 尝试完整特征集（含新衍生特征）的普通模型
        try:
            prediction = model.predict(df)
            return float(prediction[0])
        except ValueError as ve:
            # 如果模型不认识新的列名，回退到只使用原始9个基础特征
            print(f"[{datetime.now().strftime('%H:%M:%S')}] Model columns mismatch: {ve}, falling back to base features")
            base_features = pd.DataFrame([{
                'level': data.level or 0,
                'skillNum': data.skillNum or 0,
                'attackQualification': data.attackQualification or 0,
                'defenseQualification': data.defenseQualification or 0,
                'physicalQualification': data.physicalQualification or 0,
                'manaQualification': data.manaQualification or 0,
                'speedQualification': data.speedQualification or 0,
                'growUp': data.growUp or 0,
                'collect': data.collect or 0,
            }])
            prediction = model.predict(base_features)
            return float(prediction[0])

    else:  # equip / lingshi
        features = pd.DataFrame([{
            'level': data.level or 0,
            'accessoryAttributeNum': data.accessoryAttributeNum or 0,
            'primeValue': data.primeValue or 0,
            'collect': data.collect or 0
        }])
        prediction = model.predict(features)
        return float(prediction[0])


def _enhanced_fallback_logic(data: ValuationRequest) -> float:
    """
    增强版降级估值逻辑（v2）
    
    设计原则:
    1. 充分利用Java端计算的衍生特征（如果可用）
    2. 基于梦幻西游游戏经济的真实规律设计公式
    3. 多维度综合评估，避免单一维度偏差
    
    公式核心思路:
    - 技能价值: 非线性增长 + 稀有技能溢价 + 技能评分加权
    - 资质价值: 百分比映射到溢价倍率
    - 成长价值: 与等级交叉，体现高成长高等级的稀缺性
    - 宝宝溢价: 宝宝 vs 野生的巨大价差
    """

    if data.type == "pet":
        return _fallback_pet_valuation(data)
    elif data.type == "equip":
        return _fallback_equip_valuation(data)
    elif data.type == "lingshi":
        return _fallback_lingshi_valuation(data)
    else:
        raise HTTPException(status_code=400, detail="Unknown item type")


def _fallback_pet_valuation(data: ValuationRequest) -> float:
    """
    宠物增强版降级估值
    
    核心公式:
    estimated = (skill_base * rare_multiplier + qual_bonus 
                 + grow_bonus + cross_feature_bonus + heat_bonus) * baby_multiplier
    """
    # 提取基础数值（带默认值）
    level = data.level or 0
    skill_num = data.skillNum or 0
    attack_qual = data.attackQualification or 0
    defense_qual = data.defenseQualification or 0
    phys_qual = data.physicalQualification or 0
    mana_qual = data.manaQualification or 0
    speed_qual = data.speedQualification or 0
    grow_up = data.growUp or 0
    collect = data.collect or 0

    # 提取衍生特征（可能为None，需要兼容处理）
    skill_score = data.skillScore if data.skillScore is not None else (skill_num ** 1.5 * 5)
    qual_percent = data.qualPercent if data.qualPercent is not None else (attack_qual / 1600.0)
    grow_level_score = data.growLevelScore if data.growLevelScore is not None else (grow_up * level * 10)
    rare_skill_count = data.rareSkillCount if data.rareSkillCount is not None else 0
    is_baby = data.isBaby if data.isBaby is not None else 1

    # ========== 1. 技能基础价值（非线性增长） ==========
    # 技能数越多越稀缺，用平方体现非线性
    skill_base = (skill_num ** 2) * 100

    # ========== 2. 稀有技能加成 ==========
    # 每个稀有技能增加基础技能价值的30%，多个稀有技能可叠加
    # 同时考虑技能评分的直接影响
    rare_multiplier = 1.0 + (rare_skill_count * 0.3)
    skill_value_with_rare = skill_base * rare_multiplier
    score_direct_addition = skill_score * 20  # 技能评分的直接价格加成

    # ========== 3. 资质价值（百分比映射） ==========
    # 攻击资质是主要资质，其他资质按比例折算
    qual_main = attack_qual * 0.7      # 攻击资质主项
    qual_sub = speed_qual * 0.15       # 速度资质副项
    qual_other = (defense_qual + phys_qual + mana_qual) * 0.05  # 其他资质小项
    qual_total = qual_main + qual_sub + qual_other

    # 资质百分比溢价阶梯:
    # >90%: 满资级宠物，市场溢价约50%
    # >80%: 高资级，溢价25%
    # >70%: 中上资级，溢价10%
    # <70%: 正常资质，无溢价
    if qual_percent >= 0.90:
        qual_premium_rate = 0.50
    elif qual_percent >= 0.80:
        qual_premium_rate = 0.25
    elif qual_percent >= 0.70:
        qual_premium_rate = 0.10
    else:
        qual_premium_rate = 0.0

    qual_bonus = qual_total * qual_premium_rate

    # ========== 4. 成长价值 ==========
    grow_bonus = grow_up * 2500  # 每1点成长值2500金币

    # ========== 5. 成长x等级交叉特征加分 ==========
    # 高成长+高等级的宠物培养成本极高，因此更值钱
    cross_feature_bonus = grow_level_score * 50

    # ========== 6. 收藏热度（市场关注度指标） ==========
    # 高收藏量意味着市场关注度高，通常反映物品性价比
    collect_bonus = collect * 3

    # ========== 7. 宝宝乘数 ==========
    # 宝宝宠物比野生宠物价值高得多（可打书、炼妖、进阶）
    baby_multiplier = 2.5 if is_baby == 1 else 1.0

    # ========== 8. 等级调节因子 ==========
    # 高等级宠物本身就有更高基础价值
    level_factor = 1.0 + (level / 175.0) * 0.3  # 最高175级时额外30%

    # ========== 最终汇总计算 ==========
    estimated = (skill_value_with_rare + score_direct_addition
                 + qual_total + qual_bonus
                 + grow_bonus + cross_feature_bonus
                 + collect_bonus) * baby_multiplier * level_factor

    return max(estimated, 100)  # 最低估值100金币


def _fallback_equip_valuation(data: ValuationRequest) -> float:
    """
    装备降级估值（保持原有逻辑，微调参数）
    """
    level = data.level or 0
    prime_value = data.primeValue or 0
    acc_attr_num = data.accessoryAttributeNum or 0
    collect = data.collect or 0

    # 主属性价值
    base_value = level * 60 + prime_value * 6

    # 附魔属性非线性加成（附魔越多越值钱）
    acc_bonus = (acc_attr_num ** 2) * 600

    # 收藏热度
    collect_bonus = collect * 2

    estimated = base_value + acc_bonus + collect_bonus
    return max(estimated, 50)


def _fallback_lingshi_valuation(data: ValuationRequest) -> float:
    """
    灵饰降级估值（保持原有逻辑，微调参数）
    """
    level = data.level or 0
    prime_value = data.primeValue or 0
    acc_attr_num = data.accessoryAttributeNum or 0
    collect = data.collect or 0

    # 灵饰价值主要由主属性和附魔属性决定
    base_value = level * 35 + prime_value * 12

    # 三同/双相同灵饰价值远高于散件
    acc_bonus = (acc_attr_num ** 2.5) * 180

    # 收藏热度
    collect_bonus = collect * 2.5

    estimated = base_value + acc_bonus + collect_bonus
    return max(estimated, 30)


# ==================== 启动入口 ====================

if __name__ == "__main__":
    print("="*50)
    print("Mhxy Spider AI Valuation Service v2.0")
    print("Enhanced with: skillScore, qualPercent, growLevelScore, rareSkillCount, isBaby")
    print("="*50)
    uvicorn.run(app, host="0.0.0.0", port=8000)
