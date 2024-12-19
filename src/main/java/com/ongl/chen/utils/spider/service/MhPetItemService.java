package com.ongl.chen.utils.spider.service;

import com.ongl.chen.utils.spider.beans.CbgItem;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;

public interface MhPetItemService {

    public int insertOrUpdateByDetailUrl(MhPetItem mhPetItem);
}
