package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper {
	/**
	 *  插入数据
	 * @param orders
	 */
	void insert(Orders orders);
	/**
	 * 根据订单号查询订单
	 * @param orderNumber
	 */
	@Select("select * from orders where number = #{orderNumber}")
	Orders getByNumber(String orderNumber);

	/**
	 * 修改订单信息
	 * @param orders
	 */
	void update(Orders orders);

	/**
	 *  分页查询，通过订单实体对象动态
	 * @param orders
	 * @return
	 */
	Page<Orders> getByOrders(OrdersPageQueryDTO orders);

	/**
	 *  根据订单主键查询订单
	 * @return
	 */
	@Select("select * from orders where id = #{id}")
	Orders getById(Long id);

	/**
	 *  更新订单状态
	 * @param orderStatus
	 * @param orderPaidStatus
	 * @param checkOutTime
	 * @param orderId
	 */
	@Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ," +
			"checkout_time = #{checkOutTime} where id = #{orderId}")
	void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime checkOutTime, Long orderId);
}
