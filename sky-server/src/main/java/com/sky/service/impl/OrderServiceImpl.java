package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private OrderDetailMapper orderDetailMapper;
	@Autowired
	private AddressBookMapper addressBookMapper;
	@Autowired
	private ShoppingCartMapper shoppingCartMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private WeChatPayUtil weChatPayUtil;

	/** 用户下单
	 */
	@Override
	@Transactional
	public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
		// 处理各种业务异常(地址簿、地址簿为空)
		// 获取地址簿熟不
		AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
		if (addressBook == null) {
			// 地址簿为空
			throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);    // 抛出业务异常
		}
		Long userId = BaseContext.getCurrentId();
		ShoppingCart shoppingCart = ShoppingCart.builder()
				.userId(userId)
				.build();
		// 获取用户购物车数据
		List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
		if (shoppingCartList == null || shoppingCartList.size() == 0) {
			// 购物车为空
			throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
		}

		// 向订单表插入一条数据
		Orders orders = new Orders();
		BeanUtils.copyProperties(ordersSubmitDTO, orders);	// 对象拷贝
		orders.setOrderTime(LocalDateTime.now());
		orders.setPayStatus(Orders.UN_PAID);
		orders.setStatus(Orders.PENDING_PAYMENT);
		orders.setNumber(String.valueOf(System.currentTimeMillis()));
		orders.setPhone(addressBook.getPhone());
		orders.setUserId(userId);
		orderMapper.insert(orders);

		// 向订单明细表插入多条数据
		List<OrderDetail> orderDetailList = new ArrayList<>();
		for (ShoppingCart cart : shoppingCartList) {
			// 批量赋缺失的值
			OrderDetail orderDetail = new OrderDetail();
			BeanUtils.copyProperties(cart, orderDetail);
			orderDetail.setOrderId(orders.getId());	// 设置订单明细关联的订单id
			orderDetailList.add(orderDetail);
		}
		// 批量插入
		orderDetailMapper.insertBatch(orderDetailList);
		// 清空用户购物车数据
		shoppingCartMapper.deleteByUserId(userId);
		// 封装VO返回结果
		OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
				.id(orders.getId())
				.orderTime(orders.getOrderTime())
				.orderNumber(orders.getNumber())
				.orderAmount(orders.getAmount())
				.build();

		return orderSubmitVO;
	}

	/**
	 * 订单支付
	 *
	 * @param ordersPaymentDTO
	 * @return
	 */
	public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
/*		// 当前登录用户id
		Long userId = BaseContext.getCurrentId();
		User user = userMapper.getByid(userId);

		//调用微信支付接口，生成预支付交易单
		JSONObject jsonObject = weChatPayUtil.pay(
				ordersPaymentDTO.getOrderNumber(), //商户订单号
				new BigDecimal(0.01), //支付金额，单位 元
				"苍穹外卖订单", //商品描述
				user.getOpenid() //微信用户的openid
		);

		if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
			throw new OrderBusinessException("该订单已支付");
		}

		OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
		vo.setPackageStr(jsonObject.getString("package"));

		return vo;*/
		return null;
	}

	/**
	 * 支付成功，修改订单状态
	 *
	 * @param outTradeNo
	 */
	public void paySuccess(String outTradeNo) {

		// 根据订单号查询订单
		Orders ordersDB = orderMapper.getByNumber(outTradeNo);

		// 根据订单id更新订单的状态、支付方式、支付状态、结账时间
		Orders orders = Orders.builder()
				.id(ordersDB.getId())
				.status(Orders.TO_BE_CONFIRMED)
				.payStatus(Orders.PAID)
				.checkoutTime(LocalDateTime.now())
				.build();

		orderMapper.update(orders);
	}
}
