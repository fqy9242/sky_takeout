package com.fresh.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fresh.constant.MessageConstant;
import com.fresh.dto.UserLoginDTO;
import com.fresh.entity.User;
import com.fresh.exception.LoginFailedException;
import com.fresh.mapper.UserMapper;
import com.fresh.properties.WeChatProperties;
import com.fresh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
	public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
	@Autowired
	private WeChatProperties weChatProperties;
	@Autowired
	private UserMapper userMapper;
	/**
	 * 微信登录
	 *
	 */

	@Override
	public User wxLogin(UserLoginDTO userLoginDTO) {
		String openid = getOpenid(userLoginDTO.getCode());
		// 判断openid是否为空 若为空则登录失败 抛出异常
		if (openid == null) throw  new LoginFailedException(MessageConstant.LOGIN_FAILED);
		// 判断是否为新用户
		User user = userMapper.getByOpenid(openid);
		// 如果是新用户 则创建用户
		if (user == null) {
			user = User.builder()
					.openid(openid)
					.createTime(LocalDateTime.now())
					.build();
			userMapper.insert(user);
		}
		// 返回用户对象
		return user;
	}

	/**
	 *  获取用户的openid
	 * @param code
	 * @return
	 */
	private String getOpenid(String code) {
		// 调用微信接口 获取用户的openid
		String url = WX_LOGIN_URL + "?appid=" + weChatProperties.getAppid() +
				"&secret=" + weChatProperties.getSecret() +
				"&js_code=" + code +
				"&grant_type=authorization_code";
		String json = HttpUtil.get(url);
		JSONObject jsonObject = JSONUtil.parseObj(json);
		String openid = jsonObject.getStr("openid");
		return openid;
	}
}
