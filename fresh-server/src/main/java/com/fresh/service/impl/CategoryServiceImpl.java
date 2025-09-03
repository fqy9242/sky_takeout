package com.fresh.service.impl;

import com.fresh.constant.MessageConstant;
import com.fresh.constant.StatusConstant;
import com.fresh.context.BaseContext;
import com.fresh.dto.CategoryDTO;
import com.fresh.dto.CategoryPageQueryDTO;
import com.fresh.entity.Category;
import com.fresh.exception.DeletionNotAllowedException;
import com.fresh.mapper.CategoryMapper;
import com.fresh.mapper.DishMapper;
import com.fresh.mapper.SetmealMapper;
import com.fresh.result.PageResult;
import com.fresh.service.CategoryService;
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
	 * 根据类型查询分类
	 */
	@Override
	public List<Category> getByType(Integer type) {
		return categoryMapper.selectByType(type);
	}

	/**
	 * 分类分页查询
	 *
	 * @param categoryPageQueryDTO
	 * @return
	 */
	@Override
	// TODO 使用mybatis-plus 重写
	public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
//		// 设置分页
//		PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
//		Page<Category> pages = categoryMapper.selectPage(categoryPageQueryDTO);
//		// 获取总数
//		long total = pages.getTotal();
//		// 本页查询结果
//		List<Category> page = pages.getResult();
//		// 返回结果
//		PageResult pageResult = new PageResult(total, page);
//		return  pageResult;
		return null;
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
