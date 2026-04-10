package com.ongl.chen.utils.spider.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ongl.chen.utils.spider.beans.cbg.CbgAuthConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 藏宝阁全局认证配置DAO
 */
public interface CbgAuthConfigDAO extends BaseMapper<CbgAuthConfig> {

    /**
     * 获取全局认证配置（固定id=1）
     */
    @Select("SELECT * FROM cbg_auth_config WHERE id = 1 LIMIT 1")
    CbgAuthConfig getGlobalConfig();
}
