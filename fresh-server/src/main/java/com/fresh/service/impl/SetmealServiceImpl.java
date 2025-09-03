package com.fresh.service.impl;

import com.fresh.annotation.AutoFill;
import com.fresh.constant.MessageConstant;
import com.fresh.constant.StatusConstant;
import com.fresh.dto.SetmealDTO;
import com.fresh.dto.SetmealPageQueryDTO;
import com.fresh.entity.Setmeal;
import com.fresh.entity.SetmealDish;
import com.fresh.enumeration.OperationType;
import com.fresh.exception.BaseException;
import com.fresh.exception.DeletionNotAllowedException;
import com.fresh.exception.SetmealEnableFailedException;
import com.fresh.mapper.DishMapper;
import com.fresh.mapper.SetmealDishMapper;
import com.fresh.mapper.SetmealMapper;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.SetmealService;
import com.fresh.vo.DishItemVO;
import com.fresh.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
	@Autowired
	private SetmealMapper setmealMapper;
	@Autowired
	private SetmealDishMapper setmealDishMapper;
	@Autowired
	private DishMapper dishMapper;
	/**
	 * 添加套餐
	 *
	 * @param setmealDTO
	 */
	@Override
	@Transactional
	public void addSetmeal(SetmealDTO setmealDTO) {
		// 添加套餐基本信息
		Setmeal setmeal = new Setmeal();
		BeanUtils.copyProperties(setmealDTO, setmeal);
		setmealMapper.insert(setmeal);
		// 添加套餐的菜品列表
		Long setmealId = setmeal.getId();	// 套餐id
		List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();	// 欲添加套餐包含的菜品
		// 为套餐中的每个菜品绑定套餐id 然后插入套餐-菜品表
		setmealDishes.forEach(setmealDish -> {
			// 绑定套餐id
			setmealDish.setSetmealId(setmealId);
			// 插入表
			setmealDishMapper.insert(setmealDish);
		});
	}

	/**
	 * 根据id查询套餐(包含绑定菜品列表)
	 *
	 * @param id
	 * @return
	 */
	@Override
	public SetmealVO getById(long id) {
		SetmealVO setmealVO = new SetmealVO();
		Setmeal setmeal = setmealMapper.getById(id);
		if (setmeal == null) throw new BaseException(MessageConstant.SETMEAL_NOT_EXIST);
		// 拷贝对象
		BeanUtils.copyProperties(setmeal, setmealVO);
		// 从套餐绑定菜品表中 获取获取绑定的菜品
		List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
		if (setmealDishes != null && setmealDishes.size() > 0) setmealVO.setSetmealDishes(setmealDishes);
		return setmealVO;
	}

	/**
	 * 分页查询
	 *
	 * @param setmealPageQueryDTO
	 * @return
	 */
	@Override
	// TODO 使用mybatis-plus重写这里
	public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
//		PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
//		Page<SetmealVO> pages = setmealMapper.pageQuery(setmealPageQueryDTO);
//		return new PageResult(pages.getTotal(), pages.getResult());
		return  null;
	}

	/**
	 * 修改订单状态
	 *
	 * @param status
	 * @param id
	 */
	@Override
	public void startOrStop(int status, long id) {
		// 若套餐中的菜品有处于停售状态 则不可启用套餐
		if (status == StatusConstant.ENABLE) {
			List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
			setmealDishes.forEach(setmealDish -> {
				Long dishId = setmealDish.getDishId();
				boolean flag = dishMapper.selectById(dishId).getStatus() == StatusConstant.DISABLE;
				if (flag) throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
			});
		}
		Setmeal setmeal = Setmeal.builder()
				.status(status)
				.id(id)
				.build();
		setmealMapper.update(setmeal);
	}

	/**
	 * 修改套餐
	 *
	 * @param setmealDTO
	 */
	@Override
	@Transactional
	public void update(SetmealDTO setmealDTO) {
		// 修改套餐表中的信息
		Setmeal setmeal = new Setmeal();
		BeanUtils.copyProperties(setmealDTO, setmeal);
		setmealMapper.update(setmeal);
		// 删除套餐绑定的菜品 然后再重新插入信息
		Long setmealId = setmeal.getId(); // 套餐id
		// 删掉
		setmealDishMapper.deleteBySetmealId(setmealId);
		// 插入
		List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
		setmealDishes.forEach(setmealDish -> {
			setmealDish.setSetmealId(setmealId);
			setmealDishMapper.insert(setmealDish);
		});

	}

	/**
	 * 批量删除套餐
	 *
	 * @param ids
	 */
	@Override
	public void deleteBatch(List<Long> ids) {
		// 如果存在在售套餐 则不可删除
		ids.forEach(id -> {
			if (setmealMapper.getById(id).getStatus() == StatusConstant.ENABLE) {
				throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
			}
		});
		setmealMapper.deleteBatch(ids);
	}

	/**
	 * 条件查询
	 * @param setmeal
	 * @return
	 */
	@Override
	public List<Setmeal> list(Setmeal setmeal) {
		List<Setmeal> list = setmealMapper.list(setmeal);
		return list;
	}

	/**
	 * 根据id查询菜品选项
	 * @param id
	 * @return
	 */
	@Override
	public List<DishItemVO> getDishItemById(Long id) {
		return setmealMapper.getDishItemBySetmealId(id);
	}


}
