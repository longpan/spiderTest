package com.ongl.chen.utils.spider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ongl.chen.utils.spider.beans.CbgItem;
import com.ongl.chen.utils.spider.dao.CbgItemDAO;
import com.ongl.chen.utils.spider.service.CbgItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CbgItemServiceImpl  implements CbgItemService {

    @Autowired
    private CbgItemDAO cbgItemDAO;

    public int insertByCode(CbgItem cbgItem){
        CbgItem cbgItemDb = cbgItemDAO.selectOne(new QueryWrapper<CbgItem>().eq("code", cbgItem.getCode()));
        if(cbgItemDb == null) {
            cbgItemDAO.insert(cbgItem);
            return 1;
        }
        return -1;
    }
}
