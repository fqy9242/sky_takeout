package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
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
	private Long orderId;

	/** 用户下单
	 */
	@Override
	@Transactional
	public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
		// 处理各种业务异常(地址簿、地址簿为空)
		// 获取地址簿
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
		orders.setAddress(addressBook.getDetail());
		orders.setConsignee(addressBook.getConsignee());
		orderMapper.insert(orders);
		orderId = orders.getId();	// 为全局变量赋值，方面下边的方法使用
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
		// 当前登录用户id
/*		Long userId = BaseContext.getCurrentId();
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
		}*/
		// 没那实力，故跳过微信支付
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code", "ORDERPAID");
		// 源
		OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
		vo.setPackageStr(jsonObject.getString("package"));
		//为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
		Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
		Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
		//发现没有将支付时间 check_out属性赋值，所以在这里更新
		LocalDateTime check_out_time = LocalDateTime.now();
		log.info("自己将订单修改为已支付,获取到的订单id:{}", orderId);
		orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderId);

		return vo;
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

	/**
	 * 分页查询历史订单
	 *
	 * @param ordersPageQueryDTO
	 * @return
	 */
	@Override
	public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
		PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());	// 设置分页数据
		Orders orders = Orders.builder()
				.userId(BaseContext.getCurrentId())
				.status(ordersPageQueryDTO.getStatus())
				.build();
		List<OrderVO> orderVOList = new ArrayList<>();
		// 开始查询
		Page<Orders> page = orderMapper.getByOrders(ordersPageQueryDTO);
		// 查询订单明细
		if (page != null && page.getTotal() > 0) {
			for (Orders order : page) {
				Long orderId = order.getId();	// 订单id
				// 查询订单明细
				List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
				OrderVO orderVO = new OrderVO();
				BeanUtils.copyProperties(order, orderVO);
				orderVO.setOrderDetailList(orderDetails);
				orderVOList.add(orderVO);	// 保存到列表
			}
		} else {
			return null;
		}
		return new PageResult(page.getTotal(), orderVOList);
	}

	/**
	 * 查询订单详情
	 *
	 * @param id
	 * @return
	 */
	@Override
	public OrderVO detail(Long id) {
		// 根据订单主键获取订单
		Orders orders = orderMapper.getById(id);
		// 根据订单主键查询订单详细
		List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
		// 封装到vo并返回
		OrderVO orderVO = new OrderVO();
		BeanUtils.copyProperties(orders, orderVO);
		orderVO.setOrderDetailList(orderDetails);
		return orderVO;
	}

	/**
	 * @param id
	 */
	@Override
	public void cancelOrder(Long id) {
		// 根据id查询订单
		Orders ordersDB = orderMapper.getById(id);
		// 校验订单是否存在
		if (ordersDB == null) {
			throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
		}
		//订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
		if (ordersDB.getStatus() > 2) {
			throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
		}
		Orders orders = new Orders();
		orders.setId(ordersDB.getId());
		// 订单处于待接单状态下取消，需要进行退款
		/*if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
			//调用微信支付退款接口
			weChatPayUtil.refund(
					ordersDB.getNumber(), //商户订单号
					ordersDB.getNumber(), //商户退款单号
					new BigDecimal(0.01),//退款金额，单位 元
					new BigDecimal(0.01));//原订单金额

			//支付状态修改为 退款
			orders.setPayStatus(Orders.REFUND);
		}*/
		//支付状态修改为 退款
		orders.setPayStatus(Orders.REFUND);
		// 更新订单状态、取消原因、取消时间
		orders.setStatus(Orders.CANCELLED);
		orders.setCancelReason("用户取消");
		orders.setCancelTime(LocalDateTime.now());
		orderMapper.update(orders);

	}

	/**
	 * 再来一单 => 将那些玩意重新加入购物车
	 *
	 * @param id
	 */
	@Override
	public void repetition(Long id) {
		// 查询当前用户id
		Long userId = BaseContext.getCurrentId();
		// 根据订单id查询当前订单详情
		List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

		// 将订单详情对象转换为购物车对象
		List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
			ShoppingCart shoppingCart = new ShoppingCart();
			// 将原订单详情里面的菜品信息重新复制到购物车对象中
			BeanUtils.copyProperties(x, shoppingCart, "id");
			shoppingCart.setUserId(userId);
			shoppingCart.setCreateTime(LocalDateTime.now());
			return shoppingCart;
		}).collect(Collectors.toList());
		// 将购物车对象批量添加到数据库
		shoppingCartMapper.insertBatch(shoppingCartList);
	}
}
