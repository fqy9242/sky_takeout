package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
/*   分类相关控制层
 */
@Slf4j
@Api("菜品分类相关")

public class CategoryController {
	// 菜品服务层ICO容器
	@Autowired
	private CategoryService categoryService;
	/**
	 *  添加菜品分类
	 * @param categoryDTO
	 * @return
	 */
	@ApiOperation("添加菜品分类")
	@PostMapping
	public Result save(@RequestBody CategoryDTO categoryDTO) {
		categoryService.save(categoryDTO);
		return Result.success();
	}

	/**
	 *  根据类型查询分类
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperation("根据类型查询分类")
	public Result<List<Category>> getByType(String type) {
		List<Category> categories = categoryService.getByType(type);
		return Result.success(categories);
	}

	/**
	 *  分类分页查询
	 * @param categoryPageQueryDTO
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("分类分页查询")
	public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
		PageResult page = categoryService.page(categoryPageQueryDTO);
		log.info("分类分页查询,参数{}", categoryPageQueryDTO);
		return Result.success(page);
	}

	/**
	 * 修改分类
	 * @param categoryDTO
	 * @return
	 */
	@ApiOperation("修改分类")
	@PutMapping
	public Result update(@RequestBody CategoryDTO categoryDTO) {
		categoryService.update(categoryDTO);
		return Result.success();
	}

	/**
	 *  修改分类状态
	 * @param status
	 * @param id
	 * @return
	 */
	@ApiOperation("修改分类状态")
	@PostMapping("/status/{status}")
	public Result startOrStop(@PathVariable Integer status, long id) {
		log.info("修改分类状态:status = {},id = {}", status, id);
		categoryService.startOrStop(status, id);
		return Result.success();
	}

	/**
	 *  根据id删除分类
	 * @param id 分类id
	 * @return
	 */
	@DeleteMapping
	@ApiOperation("根据id删除分类")
	public Result delete(long id) {
		categoryService.deleteById(id);
		return Result.success();
	}
}
