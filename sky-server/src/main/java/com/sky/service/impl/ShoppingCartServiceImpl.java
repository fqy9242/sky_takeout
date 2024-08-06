package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
	@Autowired
	private ShoppingCartMapper shoppingCartMapper;
	@Autowired
	private DishMapper dishMapper;
	@Autowired
	private SetmealMapper setmealMapper;

	/**
	 * 添加购物车
	 *
	 * @param shoppingCartDTO
	 */
	@Override
	public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
		ShoppingCart shoppingCart = new ShoppingCart();
		BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
		Long userId = BaseContext.getCurrentId();
		shoppingCart.setUserId(userId);
		List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
		// 判断当前加入购物车中的商品是否已经存在
		if (list != null && list.size() > 0) {
			// 已经存在 数量+ 1
			ShoppingCart cart = list.get(0);
			cart.setNumber(cart.getNumber() + 1);
			shoppingCartMapper.updateNumberById(cart);
		} else {
			// 不存在 购物车加入一条数据
			// 判断这次添加到购物车的是菜品还是套餐
			Long dishId = shoppingCartDTO.getDishId();
			if (dishId != null) {
				// 添加到购物车的是菜品
				Dish dish = dishMapper.selectById(dishId);
				shoppingCart.setName(dish.getName());
				shoppingCart.setImage(dish.getImage());
				shoppingCart.setAmount(dish.getPrice());

			} else {
				// 添加到购物车的是套餐
				Long setmealId = shoppingCartDTO.getSetmealId();
				Setmeal setmeal = setmealMapper.getById(setmealId);
				shoppingCart.setName(setmeal.getName());
				shoppingCart.setImage(setmeal.getImage());
				shoppingCart.setAmount(setmeal.getPrice());
			}
			shoppingCart.setNumber(1);
			shoppingCart.setCreateTime(LocalDateTime.now());
			shoppingCartMapper.insert(shoppingCart);
		}

	}

	/**
	 * 查看购物车
	 *
	 * @return
	 */
	@Override
	public List<ShoppingCart> showShoppingCart() {
		// 获取当前微信用户的id
		Long userId = BaseContext.getCurrentId();
		ShoppingCart shoppingCart = ShoppingCart.builder()
				.userId(userId)
				.build();
		return shoppingCartMapper.list(shoppingCart);
	}

	/**
	 * 清空购物车
	 */
	@Override
	public void cleanShoppingCart() {
		shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
	}

	/**
	 * 购物车数量减少一
	 *
	 * @param shoppingCartDTO
	 */
	@Override
	public void sub(ShoppingCartDTO shoppingCartDTO) {
		ShoppingCart shoppingCart = new ShoppingCart();
		BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
		// 设置当前登录的用户id
		shoppingCart.setUserId(BaseContext.getCurrentId());
		List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
		ShoppingCart cart = list.get(0); // 获取符合条件的那一商品
		// 数量 - 1
		cart.setNumber(cart.getNumber() - 1);
		shoppingCartMapper.updateNumberById(cart);
	}
}
