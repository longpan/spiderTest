package com.ongl.chen.utils.spider;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ongl.chen.utils.spider.beans.UserInfo;
import com.ongl.chen.utils.spider.hbasedao.UserInfoMapper;
//import com.ongl.chen.utils.spider.utils.PhoenixDataSourceConfig;


@RunWith(SpringJUnit4ClassRunner.class)
//@Import(PhoenixDataSourceConfig.class)
@ContextConfiguration("classpath:/spring/spring*.xml")
//@PropertySource("classpath:mybatis/application.properties")
//@ComponentScan("com.ongl.chen.utils.spider.**")
//@MapperScan("com.ongl.chen.utils.spider.**")
public class BaseTest {

  @Autowired
  UserInfoMapper userInfoMapper;

  @Test
  public void addUser() {
    UserInfo userInfo = new UserInfo();
    userInfo.setId(2);
    userInfo.setName("Jerry");
    userInfoMapper.addUser(userInfo);
  }

  @Test
  public void getUserById() {
    UserInfo userInfo = userInfoMapper.getUserById(2);
    System.out.println(String.format("ID=%s;NAME=%s", userInfo.getId(), userInfo.getName()));
  }

  @Test
  public void getUserByName() {
    UserInfo userInfo = userInfoMapper.getUserByName("Jerry");
    System.out.println(String.format("ID=%s;NAME=%s", userInfo.getId(), userInfo.getName()));
  }

  @Test
  public void deleteUser() {
    userInfoMapper.deleteUser(1);

    List<UserInfo> userInfos = userInfoMapper.getUsers();
    for (UserInfo userInfo : userInfos) {
      System.out.println(String.format("ID=%s;NAME=%s", userInfo.getId(), userInfo.getName()));
    }
  }
}
