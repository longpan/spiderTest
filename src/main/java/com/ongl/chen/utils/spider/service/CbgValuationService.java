package com.ongl.chen.utils.spider.service;

import com.ongl.chen.utils.spider.beans.CbgItem;

/**
 * AI 数据价值挖掘服务接口
 * 用于对藏宝阁抓取的数据进行估值和性价比分析
 */
public interface CbgValuationService {

    /**
     * 对抓取的藏宝阁项进行 AI 估值
     * @param item 抓取到的原始数据
     */
    void valuate(CbgItem item);
}
