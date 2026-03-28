from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, Dict, Any
import uvicorn
import joblib
import pandas as pd
import os

app = FastAPI(title="Mhxy Spider AI Valuation Service")

# 1. 模型加载路径 (相对于脚本文件所在目录)
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_DIR = os.path.join(BASE_DIR, 'train_model')

# 2. 预加载模型 (如果文件不存在，将降级为模拟逻辑)
models = {}
try:
    models['pet'] = joblib.load(os.path.join(MODEL_DIR, 'mh_pet_model.pkl'))
    models['equip'] = joblib.load(os.path.join(MODEL_DIR, 'mh_equip_model.pkl'))
    models['lingshi'] = joblib.load(os.path.join(MODEL_DIR, 'mh_lingshi_model.pkl'))
    print(f"All trained models loaded successfully from: {MODEL_DIR}")
except Exception as e:
    print(f"Warning: Failed to load models, falling back to mock logic. Error: {e}")

# 数据模型定义
class ValuationRequest(BaseModel):
    type: str  # 'pet', 'equip', 'lingshi'
    level: float
    collect: float
    # Pet 专属特征
    skillNum: Optional[int] = None
    attackQualification: Optional[float] = None
    defenseQualification: Optional[float] = None
    physicalQualification: Optional[float] = None
    manaQualification: Optional[float] = None
    speedQualification: Optional[float] = None
    growUp: Optional[float] = None
    # Equip/LingShi 专属特征
    accessoryAttributeNum: Optional[int] = None
    primeValue: Optional[float] = None

@app.post("/predict")
async def predict(data: ValuationRequest):
    """
    接收 Java 端发送的特征，返回估值结果。
    优先使用已训练的模型，失败则使用降级逻辑。
    """
    try:
        model = models.get(data.type)
        
        # 3. 如果模型存在，执行预测
        if model:
            if data.type == "pet":
                features = pd.DataFrame([{
                    'level': data.level,
                    'skillNum': data.skillNum or 0,
                    'attackQualification': data.attackQualification or 0,
                    'defenseQualification': data.defenseQualification or 0,
                    'physicalQualification': data.physicalQualification or 0,
                    'manaQualification': data.manaQualification or 0,
                    'speedQualification': data.speedQualification or 0,
                    'growUp': data.growUp or 0,
                    'collect': data.collect
                }])
            else:  # equip or lingshi
                features = pd.DataFrame([{
                    'level': data.level,
                    'accessoryAttributeNum': data.accessoryAttributeNum or 0,
                    'primeValue': data.primeValue or 0,
                    'collect': data.collect
                }])
            
            prediction = model.predict(features)
            return {"estimated_price": round(float(prediction[0]), 2), "source": "AI_Model"}

        # 4. 模拟降级逻辑 (模型加载失败时使用)
        if data.type == "pet":
            base = (data.attackQualification or 0) * 0.5 + (data.growUp or 0) * 2000
            skills_bonus = (data.skillNum or 0) ** 2 * 100
            estimated_price = base + skills_bonus + data.collect * 5
        elif data.type == "equip":
            estimated_price = data.level * 50 + (data.primeValue or 0) * 5 + (data.accessoryAttributeNum or 0) * 800
        elif data.type == "lingshi":
            estimated_price = data.level * 30 + (data.accessoryAttributeNum or 0) ** 3 * 200 + (data.primeValue or 0) * 10
        else:
            raise HTTPException(status_code=400, detail="Unknown item type")

        return {"estimated_price": round(estimated_price, 2), "source": "Mock_Logic"}

    except Exception as e:
        return {"error": str(e), "estimated_price": None}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
