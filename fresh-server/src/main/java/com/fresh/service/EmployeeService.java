package com.fresh.service;

import com.fresh.dto.EmployeeDTO;
import com.fresh.dto.EmployeeLoginDTO;
import com.fresh.dto.EmployeePageQueryDTO;
import com.fresh.entity.Employee;
import com.fresh.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

	/**
	 *  新增员工
	 * @param employeeDTO
	 */
	void save(EmployeeDTO employeeDTO);

	/**
	 * 员工分页查询
	 *
	 * @param employeePageQueryDTO
	 * @return
	 */
	PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

	/**
	 *  启用或禁用员工账户
	 * @param status
	 * @param id
	 */
	void starOrStop(Integer status, Long id);

	/**
	 *  根据id查询员工信息
	 * @return 员工实体对象
	 */
	Employee getById(long id);

	/**
	 *  修改员工信息
	 * @param employeeDTO
	 */
	void update(EmployeeDTO employeeDTO);
}
