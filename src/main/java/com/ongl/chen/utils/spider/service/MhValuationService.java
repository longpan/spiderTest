package com.ongl.chen.utils.spider.service;

import com.ongl.chen.utils.spider.beans.dbg.MhEquipItem;
import com.ongl.chen.utils.spider.beans.dbg.MhLingShiItem;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;

/**
 * 梦幻西游物品 AI 估值服务
 */
public interface MhValuationService {

    /**
     * 对宠物进行估值
     */
    void valuatePet(MhPetItem item);

    /**
     * 对装备进行估值
     */
    void valuateEquip(MhEquipItem item);

    /**
     * 对灵饰进行估值
     */
    void valuateLingShi(MhLingShiItem item);
}
