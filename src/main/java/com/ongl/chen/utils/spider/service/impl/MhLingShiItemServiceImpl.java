package com.ongl.chen.utils.spider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ongl.chen.utils.spider.beans.dbg.MhLingShiItem;
import com.ongl.chen.utils.spider.dao.MhLingShiItemDAO;
import com.ongl.chen.utils.spider.service.MhLingShiItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MhLingShiItemServiceImpl implements MhLingShiItemService {

    @Autowired
    private MhLingShiItemDAO mhLingShiItemDAO;

    public int insertOrUpdateByDetailUrl(MhLingShiItem mhLingShiItem){
        MhLingShiItem mhLingShiItemDb = mhLingShiItemDAO.selectOne(new QueryWrapper<MhLingShiItem>().eq("detailUrl", mhLingShiItem.getDetailUrl()));
        if(mhLingShiItemDb == null) {
            mhLingShiItem.setCreateTime(new Date());
            mhLingShiItem.setUpdateTime(new Date());

            mhLingShiItemDAO.insert(mhLingShiItem);

            return 1;
        }else {
            mhLingShiItem.setId(mhLingShiItemDb.getId());
            mhLingShiItem.setUpdateTime(new Date());
            mhLingShiItemDAO.updateById(mhLingShiItem);
            return 2;
        }
    }
}
