package com.fresh.service;

import com.fresh.dto.UserLoginDTO;
import com.fresh.entity.User;

public interface UserService {
	/**
	 *  微信登录
	 * @param userLoginDTO
	 * @return
	 */
	User wxLogin(UserLoginDTO userLoginDTO);
}
