package com.ongl.chen.utils.spider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ongl.chen.utils.spider.beans.CbgItem;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;
import com.ongl.chen.utils.spider.dao.CbgItemDAO;
import com.ongl.chen.utils.spider.dao.MhPetItemDAO;
import com.ongl.chen.utils.spider.service.CbgItemService;
import com.ongl.chen.utils.spider.service.MhPetItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MhPetItemServiceImpl implements MhPetItemService {

    @Autowired
    private MhPetItemDAO mhPetItemDAO;

    public int insertOrUpdateByDetailUrl(MhPetItem mhPetItem){
        MhPetItem mhPetItemDb = mhPetItemDAO.selectOne(new QueryWrapper<MhPetItem>().eq("detailUrl", mhPetItem.getDetailUrl()));
        if(mhPetItemDb == null) {
            mhPetItem.setCreateTime(new Date());
            mhPetItem.setUpdateTime(new Date());

            mhPetItemDAO.insert(mhPetItem);

            return 1;
        }else {
            mhPetItem.setId(mhPetItemDb.getId());
            mhPetItem.setUpdateTime(new Date());
            mhPetItemDAO.updateById(mhPetItem);
            return 2;
        }
    }
}
