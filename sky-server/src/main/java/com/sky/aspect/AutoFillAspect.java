package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 *  切面类 实现公共字段自动处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
	/**
	 *  切入点
	 */
	@Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
	public void autoFillPointCut(){}

	/**
	 *  前置通知 在通知中进行公共字段的赋值
	 */
	@Before("autoFillPointCut()")
	public void autoFill(JoinPoint joinPoint) {
		log.info("开始进行公共字段的自动填充");
		// 准备赋值的数据
		LocalDateTime now = LocalDateTime.now();
		Long currentId = BaseContext.getCurrentId();
		// 获取当前被拦截方法的操作类型
		MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 获得方法签名
		AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); // 获得方法上的注解签名
		OperationType operationType = autoFill.value();	// 获得数据库操作类型
		// 获取当前被拦截方法的参数(实体对象)
		Object[] args = joinPoint.getArgs(); // 获得方法的所有参数
		if (args == null || args.length == 0) return;
		Object entry = args[0];	// 获取实体对象参数
		// 根据当前不同的操作类型 为相应的属性通过反射来赋值
		try {
			if (operationType == OperationType.INSERT) {

				// 插入
				Method setCreateTime = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class); // 创建时间
				Method setCreateUser = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class); // 创建人
				// 赋值
				setCreateTime.invoke(entry, now);
				setCreateUser.invoke(entry, currentId);
			}
			// 执行插入/更新都要处理的数据
			Method setUpdateTime = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class); // 更新时间
			Method setUpdateUser = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);  // 更新操作人
			// 赋值
			setUpdateTime.invoke(entry, now);
			setUpdateUser.invoke(entry, currentId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}


	}
}
