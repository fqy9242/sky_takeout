package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  菜品分类服务层实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {
	@Autowired
	private CategoryMapper categoryMapper;
	@Autowired
	private DishMapper dishMapper;
	@Autowired
	private SetmealMapper setmealMapper;

	/**
	 * 添加菜品分类
	 *
	 * @param categoryDTO
	 */
	@Override
	public void save(CategoryDTO categoryDTO) {
		// 将dot转为实体层并对数据进行处理
		Category category = new Category();
		// 拷贝对象
		BeanUtils.copyProperties(categoryDTO, category);
		// 设置分类状态 默认禁用
		category.setStatus(StatusConstant.DISABLE);
		categoryMapper.insert(category);
	}

	/**
	 * 查询菜品列表
	 *
	 * @param type
	 * @return
	 */
	@Override
	public List<Category> getByType(String type) {
		return categoryMapper.selectByType(type);
	}

	/**
	 * 分类分页查询
	 *
	 * @param categoryPageQueryDTO
	 * @return
	 */
	@Override
	public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
		// 设置分页
		PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
		Page<Category> pages = categoryMapper.selectPage(categoryPageQueryDTO);
		// 获取总数
		long total = pages.getTotal();
		// 本页查询结果
		List<Category> page = pages.getResult();
		// 返回结果
		PageResult pageResult = new PageResult(total, page);
		return  pageResult;
	}

	/**
	 * 修改分类
	 *
	 * @param categoryDTO
	 */
	@Override
	public void update(CategoryDTO categoryDTO) {
		Category category = new Category();
		// 拷贝对象
		BeanUtils.copyProperties(categoryDTO, category);
		// 提交给mapper层处理
		categoryMapper.update(category);
	}

	/**
	 * 修改分类状态
	 *
	 * @param status
	 * @param id
	 */
	@Override
	public void startOrStop(Integer status, long id) {
		Category category = Category.builder()
				.id(id)
				.status(status)
				.build();
		categoryMapper.update(category);
	}

	/**
	 * 根据id删除分类
	 *
	 * @param id
	 */
	@Override
	public void deleteById(long id) {
		//查询当前分类是否关联了菜品，如果关联了就抛出业务异常
		Integer count = dishMapper.countByCategoryId(id);
		if(count > 0){
			//当前分类下有菜品，不能删除
			throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
		}
		//查询当前分类是否关联了套餐，如果关联了就抛出业务异常
		count = setmealMapper.countByCategoryId(id);
		if(count > 0){
			//当前分类下有菜品，不能删除
			throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
		}
		//删除分类数据
		categoryMapper.deleteById(id);
	}
}
