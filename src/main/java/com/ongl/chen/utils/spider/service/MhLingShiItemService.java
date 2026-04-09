package com.ongl.chen.utils.spider.service;

import com.ongl.chen.utils.spider.beans.dbg.MhLingShiItem;
import com.ongl.chen.utils.spider.beans.dbg.MhPetItem;

public interface MhLingShiItemService {

    public int insertOrUpdateByDetailUrl(MhLingShiItem mhLingShiItem);
}
