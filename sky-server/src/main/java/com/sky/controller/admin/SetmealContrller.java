package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealOverViewVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealContrller {
	@Autowired
	private SetmealService setmealService;

	/**
	 *  添加套餐
	 */
	@ApiOperation("添加套餐")
	@PostMapping
	public Result add(@RequestBody SetmealDTO setmealDTO) {
		log.info("添加菜品:{}", setmealDTO);
		setmealService.addSetmeal(setmealDTO);
		return Result.success();
	}

	/**
	 *  根据id查询套餐
	 * @param id
	 * @return
	 */
	@ApiOperation("根据id查询套餐")
	@GetMapping("/{id}")
	public Result<SetmealVO> getById(@PathVariable long id) {
		log.info("根据id查询套餐:{}", id);
		SetmealVO setmealVO = setmealService.getById(id);
		return Result.success(setmealVO);
	}

	/**
	 *  分页查询
	 * @param setmealPageQueryDTO
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("分页查询")
	public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
		log.info("分页查询,{}", setmealPageQueryDTO);
		PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
		return Result.success(pageResult);
	}

	/**
	 *  修改套餐状态
	 * @param status
	 * @param id
	 * @return
	 */
	@ApiOperation("修改套餐状态")
	@PostMapping("/status/{status}")
	public Result startOrStop(@PathVariable Integer status, Long id) {
		log.info("修改套餐状态:{},{}", status, id);
		setmealService.startOrStop(status, id);
		return Result.success();
	}

	/**
	 *  修改套餐
	 */
	@ApiOperation("修改套餐")
	@PutMapping
	public Result update(@RequestBody SetmealDTO setmealDTO) {
		log.info("修改套餐:{}", setmealDTO);
		setmealService.update(setmealDTO);
		return Result.success();
	}

	/**
	 *  批量删除
	 * @param ids
	 * @return
	 */
	@ApiOperation("批量删除套餐")
	@DeleteMapping
	public Result delete(@RequestParam List<Long> ids) {
		log.info("批量删除套餐,{}", ids);
		setmealService.deleteBatch(ids);
		return Result.success();
	}
}
