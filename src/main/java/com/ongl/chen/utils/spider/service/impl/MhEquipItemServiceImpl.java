package com.ongl.chen.utils.spider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ongl.chen.utils.spider.beans.dbg.MhEquipItem;
import com.ongl.chen.utils.spider.dao.MhEquipItemDAO;
import com.ongl.chen.utils.spider.service.MhEquipItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MhEquipItemServiceImpl implements MhEquipItemService {

    @Autowired
    private MhEquipItemDAO mhEquipItemDAO;

    public int insertOrUpdateByDetailUrl(MhEquipItem mhEquipItem){
        MhEquipItem mhEquipItemDb = mhEquipItemDAO.selectOne(new QueryWrapper<MhEquipItem>().eq("detailUrl", mhEquipItem.getDetailUrl()));
        if(mhEquipItemDb == null) {
            mhEquipItem.setCreateTime(new Date());
            mhEquipItem.setUpdateTime(new Date());

            mhEquipItemDAO.insert(mhEquipItem);

            return 1;
        }else {
            mhEquipItem.setId(mhEquipItemDb.getId());
            mhEquipItem.setUpdateTime(new Date());
            mhEquipItemDAO.updateById(mhEquipItem);
            return 2;
        }
    }
}
