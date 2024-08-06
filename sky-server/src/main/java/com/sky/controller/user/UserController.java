package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user/user")
@Api(tags = "用户端用户相关接口")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private JwtProperties jwtProperties;
	/**
	 *  用户登录
	 * @param userLoginDTO
	 * @return
	 */
	@ApiOperation("用户登录")
	@PostMapping("/login")
	public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
		log.info("用户登录:{}", userLoginDTO);
		// 微信登录
		User user = userService.wxLogin(userLoginDTO);
		// 生成jwt令牌
		Map<String, Object> clams = new HashMap<>();
		clams.put(JwtClaimsConstant.USER_ID, user.getId());
		String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), clams);
		UserLoginVO userLoginVO = UserLoginVO.builder()
				.id(user.getId())
				.openid(user.getOpenid())
				.token(jwt)
				.build();
		return Result.success(userLoginVO);
	}
}
