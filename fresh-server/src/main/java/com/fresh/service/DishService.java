package com.fresh.service;

import com.fresh.dto.DishDTO;
import com.fresh.dto.DishPageQueryDTO;
import com.fresh.entity.Dish;
import com.fresh.result.PageResult;
import com.fresh.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DishService {

	/**
	 *  添加菜品和对应的口味
	 * @param dishDTO
	 */
	void saveWithFlavor(DishDTO dishDTO);

	/**
	 *  分页查询
	 * @param dishPageQueryDTO
	 * @return
	 */
	PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

	/**
	 *  批量删除菜品
	 * @param ids
	 */
	void deleteBatch(List<Long> ids);

	/**
	 *  根据id查询菜品和对应的口味
	 * @return
	 */
	DishVO getByIdWithFlavor(long id);

	/**
	 *  修改菜品
	 * @param dishDTO
	 */
	void updateWithFlavor(DishDTO dishDTO);

	/**
	 *  修改菜品状态
	 * @param status
	 */
	void startOrStop(Integer status, long id);

	/**
	 *  根据分类id查询菜品
	 * @param categoryId
	 * @return
	 */
	List<Dish> getBycategoryId(long categoryId);
	/**
	 * 条件查询菜品和口味
	 * @param dish
	 * @return
	 */
	List<DishVO> listWithFlavor(Dish dish);
}
