package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
		/**
		 *  添加菜品分类
		 * @param categoryDTO
		 */
		@Insert("insert into category(name, sort, type, status, create_time, update_time, create_user, update_user) " +
				"values (#{name}, #{sort}, #{type}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
		@AutoFill(OperationType.INSERT)
		void insert(Category categoryDTO);

	/**
	 *  根据类型查询分类
	 * @param type
	 * @return
	 */
//	@Select("select * from category where type = #{type}")
	List<Category> selectByType(Integer type);

	/**
	 *  根据条件分页查询
	 * @param category
	 * @return
	 */
	Page<Category> selectPage(CategoryPageQueryDTO category);

	/**
	 *  更新分类
	 * @param category
	 */
	@AutoFill(OperationType.UPDATE)
	void update(Category category);

	/**
	 *  根据id删除分类
	 * @param id
	 */
	@Delete("delete from category where id = #{id}")
	void deleteById(long id);
}
