package com.fresh.service;

import com.fresh.dto.ShoppingCartDTO;
import com.fresh.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
	/**
	 *  添加购物车
	 * @param shoppingCartDTO
	 */
	void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

	/**
	 *  查看购物车
	 * @return
	 */
	List<ShoppingCart> showShoppingCart();

	/**
	 *  清空购物车
	 */
	void cleanShoppingCart();

	/**
	 *  购物车数量减少一
	 * @param shoppingCartDTO
	 */
	void sub(ShoppingCartDTO shoppingCartDTO);
}
