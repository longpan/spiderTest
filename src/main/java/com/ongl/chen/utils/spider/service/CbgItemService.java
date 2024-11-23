package com.ongl.chen.utils.spider.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ongl.chen.utils.spider.beans.CbgItem;

public interface CbgItemService {

    public int insertByCode(CbgItem cbgItem);
}
