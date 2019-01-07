
/**
* @Title: UserInfoMapper.java
* @Package com.ongl.chen.utils.spider.dao.hbase
* @Description: TODO(用一句话描述该文件做什么)
* @author maojing.long
* @date 2019年1月7日
*/
package com.ongl.chen.utils.spider.hbasedao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.ongl.chen.utils.spider.beans.UserInfo;

/**
* @ClassName: UserInfoMapper
* @Description: TODO(这里用一句话描述这个类的作用)
* @author maojing.long
* @date 2019年1月7日
*
*/

public interface UserInfoMapper {

  @Insert("upsert into USER_INFO (ID,NAME) VALUES (#{user.id},#{user.name})")
  public void addUser(@Param("user") UserInfo userInfo);

  @Delete("delete from USER_INFO WHERE ID=#{userId}")
  public void deleteUser(@Param("userId") int userId);

  @Select("select * from USER_INFO WHERE ID=#{userId}")
//  @ResultMap("userResultMap")
  public UserInfo getUserById(@Param("userId") int userId);

  @Select("select * from USER_INFO WHERE NAME=#{userName}")
//  @ResultMap("userResultMap")
  public UserInfo getUserByName(@Param("userName") String userName);

  @Select("select * from USER_INFO")
//  @ResultMap("userResultMap")
  public List<UserInfo> getUsers();
}