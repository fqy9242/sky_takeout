package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Api(tags = "菜品相关接口")
@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {
	@Autowired
	private DishService dishService;
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 *  添加菜品
	 * @param dishDTO
	 * @return
	 */
	@ApiOperation("添加菜品接口")
	@PostMapping
	public Result save(@RequestBody DishDTO dishDTO) {
		log.info("添加菜品:{}", dishDTO);
		dishService.saveWithFlavor(dishDTO);
		// 清理redis缓存数据
		cleanCache("dish" + dishDTO.getCategoryId());
		return Result.success();
	}

	/**
	 *  分页查询
	 * @param dishPageQueryDTO
	 * @return
	 */
	@ApiOperation("分页查询")
	@GetMapping("/page")
	public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
		log.info("菜品分页查询,参数:{}", dishPageQueryDTO);
		PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
		return Result.success(pageResult);
	}

	/**
	 *  批量删除菜品
	 */
	@DeleteMapping
	@ApiOperation("批量删除菜品")
	public Result delete(@RequestParam List<Long> ids) {
		log.info("批量删除菜品:{}", ids);
		dishService.deleteBatch(ids);
		// 将所有的菜品缓存数据清理
		cleanCache("dish_*");
		return Result.success();
	}

	/**
	 *  根据di查询菜品
	 * @param id
	 * @return
	 */
	@ApiOperation("根据id查询菜品")
	@GetMapping("/{id}")
	public Result<DishVO> getById(@PathVariable long id) {
		log.info("根据id查询菜品:{}", id);
		DishVO dishVO =  dishService.getByIdWithFlavor(id);
		return Result.success(dishVO);
	}

	/**
	 *  修改菜品
	 * @return
	 */
	@ApiOperation("修改菜品")
	@PutMapping
	public Result update(@RequestBody DishDTO dishDTO) {
		log.info("修改菜品,{}", dishDTO);
		dishService.updateWithFlavor(dishDTO);
		// 将所有的菜品缓存数据清理
		cleanCache("dish_*");
		return Result.success();
	}
	@ApiOperation("修改菜品状态")
	@PostMapping("status/{status}")
	/**
	 *  修改菜品状态
	 */
	public Result startOrStop(@PathVariable Integer status, long id) {
		log.info("修改菜品状态,{},{}",status, id);
		dishService.startOrStop(status, id);
		// 将所有的菜品缓存数据清理
		cleanCache("dish_*");
		return Result.success();
	}
	@ApiOperation("根据分类id查询菜品")
	/**
	 * 根据分类id查询菜品
	 */
	@GetMapping("/list")
	public Result<List<Dish>> getByCategoryId(long categoryId) {
		log.info("根据分类id查询菜品:{}", categoryId);
		List<Dish> dishes = dishService.getBycategoryId(categoryId);
		return Result.success(dishes);
	}

	/**
	 *  清理缓存数据
	 * @param pattern
	 */
	private void cleanCache(String pattern) {
		Set keys = redisTemplate.keys(pattern);
		log.info("清理缓存: {}", keys);
		Long deletedCount = redisTemplate.delete(keys);
		log.info("成功清理缓存数量: {}", deletedCount);
	}

}
