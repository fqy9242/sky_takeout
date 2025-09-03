package com.fresh.service.impl;

import com.fresh.constant.MessageConstant;
import com.fresh.constant.PasswordConstant;
import com.fresh.constant.StatusConstant;
import com.fresh.context.BaseContext;
import com.fresh.dto.EmployeeDTO;
import com.fresh.dto.EmployeeLoginDTO;
import com.fresh.dto.EmployeePageQueryDTO;
import com.fresh.entity.Employee;
import com.fresh.exception.AccountLockedException;
import com.fresh.exception.AccountNotFoundException;
import com.fresh.exception.PasswordErrorException;
import com.fresh.mapper.EmployeeMapper;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();
        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);
        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传过来的明文密码进行md5加密数据
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        // 对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        // 设置账号状态,默认正常
        employee.setStatus(StatusConstant.ENABLE);
        // 设置默认密码 并进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employeeMapper.insert(employee);

    }

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
// TODO 使用mybatis-plus重写
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
//        // 开始分页查询
//        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
//        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
//        // 总记录数
//        long total = page.getTotal();
//        // 本页查询结果
//        List<Employee> recorder = page.getResult();
//        PageResult pageResult = new PageResult(total, recorder);
//        return pageResult;
        return null;
    }

    /**
     * 启用或禁用员工账户
     *
     * @param status
     * @param id
     */
    @Override
    public void starOrStop(Integer status, Long id) {
        // 构建一个员工实体类对象
        Employee emp = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.update(emp);
    }

    /**
     * 根据id查询员工信息
     *
     * @return 员工实体对象
     */
    @Override
    public Employee getById(long id) {
        Employee employee = employeeMapper.selectById(id);
        employee.setPassword("***");
        return employee;
    }

    /**
     * 修改员工信息
     *
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setUpdateUser(BaseContext.getCurrentId());
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(employee);
    }

}
