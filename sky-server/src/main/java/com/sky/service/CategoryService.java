package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
	/**
	 *  添加菜品分类
	 * @param categoryDTO
	 */
	void save(CategoryDTO categoryDTO);

	/**
	 *  根据类型查询菜品列表
	 * @param type
	 * @return
	 */
	List<Category> getByType(Integer type);

	/**
	 *  分类分页查询
	 * @param categoryPageQueryDTO
	 * @return
	 */
	PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

	/**
	 *  修改分类
	 * @param categoryDTO
	 */
	void update(CategoryDTO categoryDTO);

	/**
	 *  修改分类状态
	 * @param status
	 * @param id
	 */
	void startOrStop(Integer status, long id);

	/**
	 *  根据id删除分类
	 * @param id
	 */
	void deleteById(long id);
}
