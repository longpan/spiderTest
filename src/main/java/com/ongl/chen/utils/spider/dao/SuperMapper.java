package com.ongl.chen.utils.spider.dao;

import com.ongl.chen.utils.spider.common.SuperEntity;

/**
 * <p>
 * 测试自定义 mapper 父类
 * </p>
 */
public interface SuperMapper<SuperEntity> extends com.baomidou.mybatisplus.mapper.BaseMapper<com.ongl.chen.utils.spider.common.SuperEntity> {

    // 这里可以写自己的公共方法、注意不要让 mp 扫描到误以为是实体 Base 的操作
}
