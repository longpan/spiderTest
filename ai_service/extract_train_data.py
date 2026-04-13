#!/usr/bin/env python3
"""
从数据库提取真实训练数据脚本
使用MySQL数据库中的mh_pet_item表作为训练数据源
"""

import mysql.connector
import pandas as pd
import numpy as np
import os
from datetime import datetime
import json
from typing import Dict, List, Tuple, Optional

# 数据库配置
DB_CONFIG = {
    'host': '150.158.130.52',
    'port': 3306,
    'user': 'root',
    'password': 'qwer@1234',
    'database': 'spider'
}

# 输出目录
OUTPUT_DIR = 'train_data'
os.makedirs(OUTPUT_DIR, exist_ok=True)

def parse_number(value: str) -> float:
    """解析数字字符串，清理非数字字符"""
    if not value or str(value).strip() == '':
        return 0.0
    
    try:
        # 清理非数字字符（除了小数点）
        cleaned = ''.join(ch for ch in str(value) if ch.isdigit() or ch == '.' or ch == '-')
        if cleaned == '' or cleaned == '-':
            return 0.0
        return float(cleaned)
    except:
        return 0.0

def parse_boolean(value: str) -> int:
    """解析布尔值/字符串为0/1"""
    if not value:
        return 0
    
    value_str = str(value).lower().strip()
    if value_str in ['true', '是', '1', 'yes', 'y', '宝宝']:
        return 1
    elif value_str in ['false', '否', '0', 'no', 'n', '野生']:
        return 0
    
    # 尝试解析数字
    try:
        num = float(value_str)
        return 1 if num > 0 else 0
    except:
        return 0

def extract_rare_skills(skill_list: str) -> int:
    """从技能列表中提取稀有技能数量"""
    if not skill_list:
        return 0
    
    # 稀有技能关键词（来自Java端的配置）
    rare_keywords = ['须弥真言', '力劈华山', '壁垒击破', '谛听', '出其不意',
                    '观照万象', '死亡召唤', '林中鸟', '鬼魂', '夜战']
    
    count = 0
    skill_list_str = str(skill_list).lower()
    
    for keyword in rare_keywords:
        if keyword.lower() in skill_list_str:
            count += 1
    
    return count

def calculate_qual_percent(attack: float, defense: float, physical: float, 
                          mana: float, speed: float, dodge: float = 0.0) -> float:
    """计算资质百分比"""
    # 满资质参考值（来自Java端的配置）
    MAX_ATTACK = 1600.0
    MAX_DEFENSE = 1400.0
    MAX_PHYSICAL = 6500.0
    MAX_MANA = 2800.0
    MAX_SPEED = 1450.0
    MAX_DODGE = 1200.0
    
    attack_pct = attack / MAX_ATTACK if MAX_ATTACK > 0 else 0
    defense_pct = defense / MAX_DEFENSE if MAX_DEFENSE > 0 else 0
    physical_pct = physical / MAX_PHYSICAL if MAX_PHYSICAL > 0 else 0
    mana_pct = mana / MAX_MANA if MAX_MANA > 0 else 0
    speed_pct = speed / MAX_SPEED if MAX_SPEED > 0 else 0
    
    # 计算有效资质项
    qual_values = [attack_pct, defense_pct, physical_pct, mana_pct, speed_pct]
    
    # 如果有闪避资质也纳入计算
    if dodge > 0:
        dodge_pct = dodge / MAX_DODGE if MAX_DODGE > 0 else 0
        qual_values.append(dodge_pct)
    
    # 计算平均百分比
    if qual_values:
        avg_percent = sum(qual_values) / len(qual_values)
        return min(avg_percent, 1.5)  # 限制最大值避免异常值影响
    else:
        return 0.0

def calculate_skill_score(skill_list: str, skill_num: int) -> float:
    """计算技能评分（简化版）"""
    if skill_num <= 0:
        return 0.0
    
    # 技能权重映射（来自Java端的简化版）
    skill_weights = {
        '须弥真言': 50, '观照万象': 45, '力劈华山': 40, '谛听': 40, '出其不意': 35,
        '壁垒击破': 25, '死亡召唤': 25, '林中鸟': 20, '鬼魂': 15, '夜战': 12,
        '连击': 8, '必杀': 8, '偷袭': 6, '吸血': 5, '反击': 5, '反震': 5,
        '感知': 4, '隐身': 4, '敏捷': 3, '飞行': 3, '神佑复生': 10
    }
    
    total_score = 0.0
    skill_list_str = str(skill_list).lower()
    
    # 计算技能总分
    for skill, weight in skill_weights.items():
        if skill.lower() in skill_list_str:
            total_score += weight
    
    # 如果没有匹配的特殊技能，给基础分
    if total_score == 0 and skill_num > 0:
        total_score = skill_num * 1.0
    
    # 归一化处理
    return total_score / np.sqrt(skill_num) if skill_num > 0 else 0.0

def extract_pet_data() -> pd.DataFrame:
    """从数据库提取宠物数据并处理为训练数据格式"""
    
    print(f"[{datetime.now().strftime('%H:%M:%S')}] 开始连接数据库...")
    
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)
        
        print("数据库连接成功！")
        
        # 查询数据
        query = """
        SELECT 
            level, skillNum, skillList,
            attackQualification, defenseQualification, 
            physicalQualification, manaQualification, 
            speedQualification, dodgeQualification,
            growUp, collect, price,
            isBaby, name, code, createTime
        FROM mh_pet_item 
        WHERE price IS NOT NULL AND price != ''
        ORDER BY updateTime DESC
        """
        
        print("执行查询...")
        cursor.execute(query)
        rows = cursor.fetchall()
        
        print(f"查询到 {len(rows)} 条宠物数据")
        
        if len(rows) == 0:
            print("警告：数据库中未找到宠物数据！")
            return pd.DataFrame()
        
        # 处理数据
        processed_data = []
        
        for i, row in enumerate(rows):
            if i % 1000 == 0:
                print(f"正在处理数据: {i}/{len(rows)}")
            
            try:
                # 解析基础数据
                level = parse_number(row.get('level', '0'))
                skill_num = int(row.get('skillNum', 0)) if row.get('skillNum') else 0
                skill_list = row.get('skillList', '')
                
                # 解析资质数据
                attack_qual = parse_number(row.get('attackQualification', '0'))
                defense_qual = parse_number(row.get('defenseQualification', '0'))
                physical_qual = parse_number(row.get('physicalQualification', '0'))
                mana_qual = parse_number(row.get('manaQualification', '0'))
                speed_qual = parse_number(row.get('speedQualification', '0'))
                dodge_qual = parse_number(row.get('dodgeQualification', '0'))
                
                # 解析成长和收藏
                grow_up = parse_number(row.get('growUp', '0'))
                collect_val = parse_number(row.get('collect', '0'))
                price_val = parse_number(row.get('price', '0'))
                
                # 解析是否宝宝
                is_baby_val = parse_boolean(row.get('isBaby', '0'))
                
                # 计算衍生特征
                rare_skill_count = extract_rare_skills(skill_list)
                qual_percent = calculate_qual_percent(attack_qual, defense_qual, 
                                                      physical_qual, mana_qual, 
                                                      speed_qual, dodge_qual)
                skill_score = calculate_skill_score(skill_list, skill_num)
                grow_level_score = grow_up * level * 10.0
                
                # 构建训练数据行
                data_row = {
                    'level': level,
                    'skillNum': skill_num,
                    'attackQualification': attack_qual,
                    'defenseQualification': defense_qual,
                    'physicalQualification': physical_qual,
                    'manaQualification': mana_qual,
                    'speedQualification': speed_qual,
                    'growUp': grow_up,
                    'collect': collect_val,
                    'price': price_val,
                    
                    # 衍生特征
                    'rareSkillCount': rare_skill_count,
                    'qualPercent': qual_percent,
                    'skillScore': skill_score,
                    'growLevelScore': grow_level_score,
                    'isBaby': is_baby_val,
                    
                    # 原始数据（用于调试）
                    'original_skillList': str(skill_list)[:100],  # 只保留前100字符
                    'original_isBaby': str(row.get('isBaby', '')),
                    'name': row.get('name', ''),
                    'code': row.get('code', ''),
                    'createTime': row.get('createTime')
                }
                
                processed_data.append(data_row)
                
            except Exception as e:
                print(f"第 {i} 行数据处理失败: {e}")
                continue
        
        # 转换为DataFrame
        df = pd.DataFrame(processed_data)
        
        print(f"\n数据处理完成，共 {len(df)} 条有效数据")
        
        # 数据统计
        print("\n=== 数据统计 ===")
        print(f"等级范围: {df['level'].min():.0f} ~ {df['level'].max():.0f}")
        print(f"技能数范围: {df['skillNum'].min()} ~ {df['skillNum'].max()}")
        print(f"价格范围: {df['price'].min():.2f} ~ {df['price'].max():.2f}")
        print(f"收藏人数: {df['collect'].sum():.0f} 总收藏")
        print(f"宝宝数量: {df['isBaby'].sum()} (占比: {df['isBaby'].mean()*100:.1f}%)")
        
        # 检查空值
        print("\n=== 空值统计 ===")
        for col in df.columns:
            null_count = df[col].isnull().sum()
            if null_count > 0:
                print(f"{col:30}: {null_count:6} 个空值 (占比: {null_count/len(df)*100:.1f}%)")
        
        # 数据描述
        print("\n=== 关键指标描述 ===")
        numeric_cols = ['level', 'skillNum', 'attackQualification', 'growUp', 'collect', 'price']
        print(df[numeric_cols].describe())
        
        conn.close()
        print("\n数据库连接已关闭")
        
        return df
        
    except mysql.connector.Error as e:
        print(f"数据库连接错误: {e}")
        return pd.DataFrame()
    except Exception as e:
        print(f"数据处理错误: {e}")
        return pd.DataFrame()

def save_training_data(df: pd.DataFrame):
    """保存训练数据到CSV文件"""
    if len(df) == 0:
        print("错误：没有数据可保存！")
        return
    
    # 保存宠物数据
    pet_filename = os.path.join(OUTPUT_DIR, 'real_pet_train_data.csv')
    df.to_csv(pet_filename, index=False)
    print(f"\n宠物训练数据已保存到: {pet_filename}")
    print(f"数据量: {len(df)} 条记录")
    
    # 保存数据字典
    data_dict = {
        'description': '梦幻西游藏宝阁宠物真实训练数据',
        'source': 'mh_pet_item 数据库表',
        'extraction_time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        'record_count': len(df),
        'fields': {
            'level': '等级',
            'skillNum': '技能数量',
            'attackQualification': '攻击资质',
            'defenseQualification': '防御资质',
            'physicalQualification': '体力资质',
            'manaQualification': '法力资质',
            'speedQualification': '速度资质',
            'growUp': '成长值',
            'collect': '收藏人数',
            'price': '价格（目标变量）',
            'rareSkillCount': '稀有技能数量（衍生特征）',
            'qualPercent': '综合资质百分比（衍生特征）',
            'skillScore': '技能价值评分（衍生特征）',
            'growLevelScore': '成长等级评分（衍生特征）',
            'isBaby': '是否宝宝（1=是，0=否）'
        }
    }
    
    dict_filename = os.path.join(OUTPUT_DIR, 'data_dictionary.json')
    with open(dict_filename, 'w', encoding='utf-8') as f:
        json.dump(data_dict, f, ensure_ascii=False, indent=2)
    
    print(f"数据字典已保存到: {dict_filename}")
    
    # 生成数据报告
    generate_data_report(df, OUTPUT_DIR)

def generate_data_report(df: pd.DataFrame, output_dir: str):
    """生成数据质量报告"""
    
    report = {
        'summary': {
            'total_records': len(df),
            'date_extracted': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            'fields_count': len(df.columns)
        },
        'field_statistics': {},
        'quality_issues': []
    }
    
    # 检查字段统计
    for col in df.columns:
        col_stats = {
            'dtype': str(df[col].dtype),
            'non_null_count': int(df[col].notnull().sum()),  # 转换为int
            'null_count': int(df[col].isnull().sum()),  # 转换为int
            'null_percentage': float(df[col].isnull().mean() * 100),  # 转换为float
            'unique_values': int(df[col].nunique())  # 转换为int
        }
        
        if pd.api.types.is_numeric_dtype(df[col]):
            col_stats['min'] = float(df[col].min())
            col_stats['max'] = float(df[col].max())
            col_stats['mean'] = float(df[col].mean())
            col_stats['median'] = float(df[col].median())
        
        report['field_statistics'][col] = col_stats
        
        # 检查质量问题
        if col_stats['null_percentage'] > 20:
            report['quality_issues'].append(f"字段 '{col}' 空值过多: {col_stats['null_percentage']:.1f}%")
        
        if col_stats['unique_values'] == 1 and len(df) > 10:
            report['quality_issues'].append(f"字段 '{col}' 值单一: 只有1个唯一值")
    
    # 检查价格分布
    if 'price' in df.columns:
        price_stats = df['price'].describe()
        q1 = float(price_stats['25%'])
        q3 = float(price_stats['75%'])
        iqr = q3 - q1
        lower_bound = float(q1 - 1.5 * iqr)
        upper_bound = float(q3 + 1.5 * iqr)
        
        outliers = df[(df['price'] < lower_bound) | (df['price'] > upper_bound)]
        if len(outliers) > 0:
            report['quality_issues'].append(f"价格存在异常值: {int(len(outliers))} 个 (占比: {float(len(outliers)/len(df)*100):.1f}%)")
    
    # 保存报告
    report_filename = os.path.join(output_dir, 'data_quality_report.json')
    with open(report_filename, 'w', encoding='utf-8') as f:
        json.dump(report, f, ensure_ascii=False, indent=2, default=str)
    
    print(f"数据质量报告已保存到: {report_filename}")
    
    # 打印摘要
    print(f"\n=== 数据摘要 ===")
    print(f"总记录数: {len(df)}")
    print(f"字段数量: {len(df.columns)}")
    
    if report['quality_issues']:
        print(f"质量问题: {len(report['quality_issues'])} 个")
        for issue in report['quality_issues']:
            print(f"  - {issue}")
    else:
        print("数据质量良好！")

def main():
    """主函数"""
    print("=" * 80)
    print("梦幻西游藏宝阁宠物真实数据提取工具")
    print("=" * 80)
    print(f"开始时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()
    
    # 提取数据
    df = extract_pet_data()
    
    if len(df) == 0:
        print("没有提取到有效数据，程序退出。")
        return
    
    # 保存数据
    save_training_data(df)
    
    print(f"\n完成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 80)

if __name__ == "__main__":
    main()