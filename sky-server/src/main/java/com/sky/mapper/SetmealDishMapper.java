package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
	/**
	 *  根据菜品id来查询对应的套餐id
	 * @param dishIds
	 * @return
	 */
	List<Long> getSetmealIdByDishId(List<Long> dishIds);
}
