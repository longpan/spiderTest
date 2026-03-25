from fastapi import FastAPI
from pydantic import BaseModel
import joblib
import pandas as pd

app = FastAPI()

# 1. 加载模型和编码器
model = joblib.load('cbg_xgboost_model.pkl')
le = joblib.load('sect_encoder.pkl')

class ItemData(BaseModel):
    overall_score: float
    person_score: float
    level: int
    sect: str
    collect_count: int

@app.post("/predict")
def predict_price(item: ItemData):
    try:
        # 2. 预处理
        # 对门派进行编码
        sect_encoded = le.transform([item.sect])[0]
        
        # 3. 构造模型特征 (Features)
        # 顺序必须与训练时一致: ['overallScore', 'personScore', 'level', 'sect_encoded', 'collect']
        features = pd.DataFrame([{
            'overallScore': item.overall_score,
            'personScore': item.person_score,
            'level': item.level,
            'sect_encoded': sect_encoded,
            'collect': item.collect_count
        }])
        
        # 4. 执行预测
        prediction = model.predict(features)
        
        # 5. 返回估值结果 (Estimated Price)
        return {"estimated_price": float(prediction[0])}

    except Exception as e:
        return {"error": str(e), "estimated_price": None}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
