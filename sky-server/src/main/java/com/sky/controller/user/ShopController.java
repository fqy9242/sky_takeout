package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@Slf4j
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
public class ShopController {
	public static final String KEY = "SHOP_STATUS";
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 获取店铺营业状态
	 * @return
	 */
	@ApiOperation ("获取店铺营业状态")
	@GetMapping ("/status")
	public Result<Integer> getStatus() {
		Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
		log.info("获取到店铺营业状态:{}", status == StatusConstant.ENABLE ? "营业中" : "打烊中");
		return Result.success(status);
	}
}
