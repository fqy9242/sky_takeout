package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
	/**
	 *  用户下单
	 * @param ordersSubmitDTO
	 * @return
	 */
	OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
	/**
	 * 订单支付
	 * @param ordersPaymentDTO
	 * @return
	 */
	OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

	/**
	 * 支付成功，修改订单状态
	 * @param outTradeNo
	 */
	void paySuccess(String outTradeNo);

	/**
	 *  分页查询历史订单
	 * @param ordersPageQueryDTO
	 * @return
	 */
	PageResult page(OrdersPageQueryDTO ordersPageQueryDTO);

	/**
	 *  查询订单详情
	 * @param id
	 * @return
	 */
	OrderVO detail(Long id);

	void cancelOrder(Long id);

	/**
	 *  再来一单 => 将那些玩意重新加入购物车
	 * @param id
	 */
	void repetition(Long id);

	/**
	 *  订单搜索
	 * @param ordersPageQueryDTO
	 * @return
	 */
	PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

	/**
	 *  各个状态的订单数量统计
	 * @return
	 */
	OrderStatisticsVO statistics();

	/**
	 *  接单
	 * @param ordersConfirmDTO
	 */
	void confirm(OrdersConfirmDTO ordersConfirmDTO);

	/**
	 *  拒单
	 * @param ordersRejectionDTO
	 */
	void rejection(OrdersRejectionDTO ordersRejectionDTO);

	/**
	 *  取消订单
	 * @param ordersCancelDTO
	 */
	void cancel(OrdersCancelDTO ordersCancelDTO);

	void delivery(Long id);

	void complete(Long id);
	void checkOutOfRange(String address);

	/**
	 *  用户催单
	 * @param id
	 */
	void reminder(Long id);
}
