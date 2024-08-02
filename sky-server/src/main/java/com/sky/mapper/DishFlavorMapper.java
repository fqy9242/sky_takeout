package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
	/**
	 *  批量插入口味
	 * @param flavors
	 */
	void insertBatch(List<DishFlavor> flavors);

	/**
	 *  根据菜品id删除口味
	 * @param id
	 */
	@Delete("delete from dish_flavor where id = #{id}")
	void deleteByDishId(Long id);

	/**
	 *  根据菜品id批量删除口味
	 * @param ids
	 */
	void deleteByDishIds(List<Long> ids);

	/**
	 *  根据菜品id查询口味数据
	 * @param dishId
	 * @return
	 */
	@Select("select * from dish_flavor where dish_id = #{dishId}")
	List<DishFlavor> getByDishId(long dishId);
}
