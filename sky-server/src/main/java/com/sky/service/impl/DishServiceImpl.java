package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
	@Autowired
	private DishMapper dishMapper;
	@Autowired
	private DishFlavorMapper dishFlavorMapper;
	@Autowired
	private SetmealDishMapper setmealDishMapper;
	/**
	 * 添加菜品和对应的口味
	 *
	 * @param dishDTO
	 */
	@Override
	@Transactional
	public void saveWithFlavor(DishDTO dishDTO) {
		Dish dish = new Dish();
		BeanUtils.copyProperties(dishDTO, dish);
		// 向菜品表插入1条数据
		dishMapper.insert(dish);
		// 获取菜品id
		Long dishId = dish.getId();
		List<DishFlavor> flavors = dishDTO.getFlavors(); // 获取需要添加的口味集合
		if (flavors != null && flavors.size() > 0) {
			// 为每个口味都设置菜品id
			flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
			// 向口味表里插入n条数据
			dishFlavorMapper.insertBatch(flavors);
		}


	}

	/**
	 * 分页查询
	 *
	 * @param dishPageQueryDTO
	 * @return
	 */
	@Override
	public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
		PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
		Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 批量删除菜品
	 *
	 */
	@Override
	@Transactional
	public void deleteBatch(List<Long> ids) {
		// 判断需要删除的菜品中是否存在在售菜品 => refuse
		ids.forEach(id -> {
			if (dishMapper.selectById(id).getStatus() == StatusConstant.ENABLE) {
				// 存在在售菜品 抛异常
				throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
			}
		});
		// 判断需要删除的重拍吧是否绑定了套餐 => refuse
		List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishId(ids);
		if (setmealIds != null && setmealIds.size() > 0) {
			// 不给删 抛异常
			throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
		}
		// 删除菜品表中的数据
/*		ids.forEach(id -> {
			dishMapper.deleteByid(id);
			// 删除菜品表中绑定的口味数据
			dishFlavorMapper.deleteByDishId(id);
		});*/
		// 批量删除 菜品和菜品所绑定的口味
		dishMapper.deleteByIds(ids);
		dishFlavorMapper.deleteByDishIds(ids);

	}

	/**
	 * 根据id查询菜品和对应的口味
	 *
	 * @return
	 */
	@Override
	public DishVO getByIdWithFlavor(long id) {
		// 根据id查询菜品数据
		Dish dish = dishMapper.selectById(id);
		// 根据菜品id查询口味
		List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
		// 封装成VO
		DishVO dishVO = new DishVO();
		// 对象拷贝
		BeanUtils.copyProperties(dish, dishVO);
		dishVO.setFlavors(dishFlavors);
		return dishVO;
	}

	/**
	 * 修改菜品
	 *
	 * @param dishDTO
	 */
	@Override
	@Transactional
	public void updateWithFlavor(DishDTO dishDTO) {
		Dish dish = new Dish();
		BeanUtils.copyProperties(dishDTO, dish);
		// 修改菜品表基本信息
		dishMapper.update(dish);
		// 删除原有口味数据 再重新插入口味数据
		dishFlavorMapper.deleteByDishId(dishDTO.getId());
		List<DishFlavor> flavors = dishDTO.getFlavors();
		if (flavors != null && flavors.size() > 0) {
			// 为每个口味都设置菜品id
			flavors.forEach(dishFlavor -> dishFlavor.setDishId(dish.getId()));
			// 向口味表里插入n条数据
			dishFlavorMapper.insertBatch(flavors);
		}

	}

	/**
	 * 修改菜品状态
	 *
	 * @param status
	 */
	@Override
	public void startOrStop(Integer status, long id) {
		Dish dish = new Dish();
		dish.setStatus(status);
		dish.setId(id);
		dishMapper.update(dish);
	}

	/**
	 * 根据分类id查询菜品
	 *
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<Dish> getBycategoryId(long categoryId) {
		return dishMapper.getByCategoryId(categoryId);
	}
}
