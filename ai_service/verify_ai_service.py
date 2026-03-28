import requests
import json

def test_valuation(item_type, data):
    url = "http://localhost:8000/predict"
    payload = {"type": item_type, **data}
    
    print(f"\nTesting {item_type.upper()} valuation...")
    try:
        response = requests.post(url, json=payload)
        if response.status_code == 200:
            result = response.json()
            print(f"Success! Result: {json.dumps(result, indent=2, ensure_ascii=False)}")
        else:
            print(f"Failed with status code: {response.status_code}")
            print(f"Response: {response.text}")
    except Exception as e:
        print(f"Connection error: {e}")

if __name__ == "__main__":
    # 1. 测试宠物
    pet_test = {
        "level": 109,
        "skillNum": 8,
        "attackQualification": 1550,
        "defenseQualification": 1400,
        "physicalQualification": 5000,
        "manaQualification": 2500,
        "speedQualification": 1300,
        "growUp": 1.28,
        "collect": 150
    }
    test_valuation("pet", pet_test)

    # 2. 测试装备
    equip_test = {
        "level": 160,
        "accessoryAttributeNum": 2,
        "primeValue": 350,
        "collect": 45
    }
    test_valuation("equip", equip_test)

    # 3. 测试灵饰
    lingshi_test = {
        "level": 140,
        "accessoryAttributeNum": 3,
        "primeValue": 28,
        "collect": 80
    }
    test_valuation("lingshi", lingshi_test)
