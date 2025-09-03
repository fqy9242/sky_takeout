package com.fresh.mapper;

import com.fresh.annotation.AutoFill;
import com.fresh.dto.EmployeePageQueryDTO;
import com.fresh.entity.Employee;
import com.fresh.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

	/**
	 *  插入员工数据
	 * @param employee
	 */
	@Insert("insert into employee(name, username, password, phone, sex, id_number, create_time, " +
			"update_time, create_user, update_user)  values " +
			"(#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{createTime}, #{updateTime}" +
			",#{createUser}, #{updateUser})")
	@AutoFill(OperationType.INSERT)
	void insert(Employee employee);

//	/**
//	 *  分页查询
//	 * @param employeePageQueryDTO
//	 * @return
//	 */
//	Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

	/**
	 *  更新员工数据
	 * @param emp
	 */
	@AutoFill(OperationType.UPDATE)
	void update(Employee emp);

	/**
	 *  根据id查询员工信息
	 * @param id 员工id
	 * @return 员工实体对象
	 */
	@Select("select * from employee where id = #{id}")
	Employee selectById(long id);
}
