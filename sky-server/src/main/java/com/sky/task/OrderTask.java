package com.sky.task;

import com.sky.constant.CancelReasonConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
	@Autowired
	private OrderMapper orderMapper;
	/**
	 *  定时处理超时订单
	 */
	@Scheduled(cron = "0 0/1 * * * ? ")	// 每分钟执行一次
	public void processTimeoutOrder() {
		log.info("定时处理超时订单:{}", LocalDateTime.now());
		LocalDateTime dateTime = LocalDateTime.now().plusMinutes(-15);
		List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, dateTime);
		if (orderList != null && orderList.size() > 0) {
			for (Orders order : orderList) {
				order.setStatus(Orders.CANCELLED);
				order.setCancelReason(CancelReasonConstant.ORDER_TIME_OUT);
				order.setCancelTime(LocalDateTime.now());
				orderMapper.update(order);	// 更新
			}
		}
	}

	/**
	 *  处理一直处于派送中的订单
	 */
	@Scheduled(cron = "0 0 1 * * ?")	// 每天凌晨一点执行
	public void processDeliveryOrder() {
		log.info("定时处理一直处于派送中的订单:{}", LocalDateTime.now());
		LocalDateTime dateTime = LocalDateTime.now().plusMinutes(-60);	// 上一个工作日
		List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, dateTime);
		if (orderList != null && orderList.size() > 0) {
			for (Orders order : orderList) {
				order.setStatus(Orders.COMPLETED);
				orderMapper.update(order);	// 更新
			}
		}
	}

}
