package com.ongl.chen.utils.spider;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ongl.chen.utils.spider.beans.JDProductDetail;
import com.ongl.chen.utils.spider.beans.UserInfo;
import com.ongl.chen.utils.spider.hbasedao.JDProductDetailHbaseDAO;
import com.ongl.chen.utils.spider.hbasedao.UserInfoMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/spring*.xml")
public class BaseTest {

	@Autowired
	UserInfoMapper userInfoMapper;
	
	@Autowired
	JDProductDetailHbaseDAO jDProductDetailHbaseDAO;

	@Test
	public void addUser() {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(6);
		userInfo.setName("Jerry");
		userInfoMapper.addUser(userInfo);
	}
	
	@Test
	public void addProduct() {
		JDProductDetail detail = new JDProductDetail();
//		detail.setId(1L);
		detail.setpName("22222");
		jDProductDetailHbaseDAO.addProduct(detail);
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
