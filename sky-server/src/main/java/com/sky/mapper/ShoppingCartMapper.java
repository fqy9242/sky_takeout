package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
	/**
	 *  动态多条件查询
	 * @param shoppingCart
	 * @return
	 */
	List<ShoppingCart> list(ShoppingCart shoppingCart);

	/**
	 *  根据 根据id修改购物车商品数量
	 * @param shoppingCart
	 */
	@Update("update shopping_cart set number = #{number} where id = #{id}")
	void updateNumberById(ShoppingCart shoppingCart);

	/**
	 *  添加购物差对象
	 * @param shoppingCart
	 */
	@Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
			"values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
	void insert(ShoppingCart shoppingCart);

	/**
	 *  根据用户id清空购物车
	 */
	@Delete("delete from shopping_cart where user_id = #{userID}")
	void deleteByUserId(Long userId);
}
