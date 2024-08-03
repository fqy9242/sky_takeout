package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
	/**
	 *  添加套餐
	 * @param setmealDTO
	 */
	void addSetmeal(SetmealDTO setmealDTO);

	/**
	 *  根据id查询套餐(包含绑定菜品列表)
	 * @param id
	 * @return
	 */
	SetmealVO getById(long id);

	/**
	 *  分页查询
	 * @param setmealPageQueryDTO
	 * @return
	 */
	PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

	/**
	 *  修改订单状态
	 * @param status
	 * @param id
	 */
	void startOrStop(int status, long id);

	/**
	 *  修改套餐
	 * @param setmealDTO
	 */
	void update(SetmealDTO setmealDTO);

	/**
	 *  批量删除套餐
	 * @param ids
	 */
	void deleteBatch(List<Long> ids);
}
