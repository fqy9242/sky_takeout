package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
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

	/**
	 *  插入
	 * @param setmealDish
	 */

	void insert(SetmealDish setmealDish);

	/**
	 *  根据套餐id查询
	 * @param setmealId .
	 * @return
	 */
	@Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
	List<SetmealDish> getBySetmealId(long setmealId);

	/**
	 *  根据套餐id删除
	 */
	@Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
	void deleteBySetmealId(Long setmealId);

}
