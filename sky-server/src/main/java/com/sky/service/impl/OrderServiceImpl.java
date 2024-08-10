package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import io.swagger.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	@Autowired
	private WebSocketServer webSocketServer;
	private Long orderId;
	@Value("${sky.shop.address}")
	private String shopAddress;
	@Value("${sky.baidu.ak}")
	private String ak;

	/** 用户下单
	 */
	@Override
	@Transactional
	public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
		// 处理各种业务异常(购物车、地址簿为空)
		// 获取地址簿
		AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
		if (addressBook == null) {
			// 地址簿为空
			throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);    // 抛出业务异常
		}
		// 检查用户的收获地址是否超出配送范围
//		checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

		// 检查用户购物车是否为空
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



		// 没条件做支付 模拟一下
		// 通过webSocket向客户端浏览器推送消息
		Map map = new HashMap();
		map.put("type", 1);	// 1来电提醒 2.客户催单
		map.put("orderId", orderId);
		map.put("content", "订单号:" + orders.getId());
		String json = JSON.toJSONString(map);
		webSocketServer.sendToAllClient(json);

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
	/**
	 * 订单搜索
	 *
	 * @param ordersPageQueryDTO
	 * @return
	 */
	@Override
	public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
		PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
		Page<Orders> page = orderMapper.getByOrders(ordersPageQueryDTO);
		List<OrderVO> orderVOList = getOrderVOList(page);
		return new PageResult(page.getTotal(), orderVOList);
	}
	private List<OrderVO> getOrderVOList(Page<Orders> page) {
		// 需要返回订单菜品信息，自定义OrderVO响应结果
		List<OrderVO> orderVOList = new ArrayList<>();

		List<Orders> ordersList = page.getResult();
		if (!CollectionUtils.isEmpty(ordersList)) {
			for (Orders orders : ordersList) {
				// 将共同字段复制到OrderVO
				OrderVO orderVO = new OrderVO();
				BeanUtils.copyProperties(orders, orderVO);
				String orderDishes = getOrderDishesStr(orders);
				// 将订单菜品信息封装到orderVO中，并添加到orderVOList
				orderVO.setOrderDishes(orderDishes);
				orderVOList.add(orderVO);
			}
		}
		return orderVOList;
	}
	/**
	 * 根据订单id获取菜品信息字符串
	 *
	 * @param orders
	 * @return
	 */
	private String getOrderDishesStr(Orders orders) {
		// 查询订单菜品详情信息（订单中的菜品和数量）
		List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

		// 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
		List<String> orderDishList = orderDetailList.stream().map(x -> {
			String orderDish = x.getName() + "*" + x.getNumber() + ";";
			return orderDish;
		}).collect(Collectors.toList());

		// 将该订单对应的所有菜品信息拼接在一起
		return String.join("", orderDishList);
	}

	/**
	 * 各个状态的订单数量统计
	 *
	 * @return
	 */
	@Override
	public OrderStatisticsVO statistics() {
		// 待配送数量
		Integer confirmedCount =  orderMapper.countByStatus(Orders.CONFIRMED);
		// 配送中数量
		Integer deliveryInProgressCount =  orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
		// 待接单数量
		Integer toBeConfirmedCount =  orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
		// 将查询出来的数据封装成VO并返回
		OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
				.confirmed(confirmedCount)
				.deliveryInProgress(deliveryInProgressCount)
				.toBeConfirmed(toBeConfirmedCount)
				.build();
		return orderStatisticsVO;
	}

	/**
	 * 接单
	 *
	 * @param ordersConfirmDTO
	 */
	@Override
	public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
		Orders orders = Orders.builder()
				.id(ordersConfirmDTO.getId())
				.status(Orders.CONFIRMED)
				.build();

		orderMapper.update(orders);
	}

	/**
	 * 拒单
	 *
	 * @param ordersRejectionDTO
	 */
	@Override
	public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
		// 根据id查询订单
		Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());
		// 订单只有存在且状态为2（待接单）才可以拒单
		if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
			throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
		}

/*		//支付状态
		Integer payStatus = ordersDB.getPayStatus();
		if (payStatus == Orders.PAID) {
			//用户已支付，需要退款
			String refund = weChatPayUtil.refund(
					ordersDB.getNumber(),
					ordersDB.getNumber(),
					new BigDecimal(0.01),
					new BigDecimal(0.01));
			log.info("申请退款：{}", refund);
		}*/

		// 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
		Orders orders = new Orders();
		orders.setId(ordersDB.getId());
		orders.setStatus(Orders.CANCELLED);
		orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
		orders.setCancelTime(LocalDateTime.now());
		orderMapper.update(orders);
	}

	/**
	 * 取消订单
	 *
	 * @param ordersCancelDTO
	 */
	@Override
	public void cancel(OrdersCancelDTO ordersCancelDTO) {
		// 根据id查询订单
		Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

/*		//支付状态
		Integer payStatus = ordersDB.getPayStatus();
		if (payStatus == 1) {
			//用户已支付，需要退款
			String refund = weChatPayUtil.refund(
					ordersDB.getNumber(),
					ordersDB.getNumber(),
					new BigDecimal(0.01),
					new BigDecimal(0.01));
			log.info("申请退款：{}", refund);
		}*/

		// 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
		Orders orders = new Orders();
		orders.setId(ordersCancelDTO.getId());
		orders.setStatus(Orders.CANCELLED);
		orders.setCancelReason(ordersCancelDTO.getCancelReason());
		orders.setCancelTime(LocalDateTime.now());
		orderMapper.update(orders);
	}

	/**
	 * @param id
	 */
	@Override
	public void delivery(Long id) {
		// 根据id查询订单
		Orders ordersDB = orderMapper.getById(id);
		// 校验订单是否存在，并且状态为待派送
		if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
			throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
		}
		Orders orders = new Orders();
		orders.setId(ordersDB.getId());
		// 更新订单状态,状态转为派送中
		orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
		orderMapper.update(orders);
	}

	/**
	 * @param id
	 */
	@Override
	public void complete(Long id) {
		// 根据id查询订单
		Orders ordersDB = orderMapper.getById(id);

		// 校验订单是否存在，并且状态为4
		if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
			throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
		}
		Orders orders = new Orders();
		orders.setId(ordersDB.getId());
		// 更新订单状态,状态转为完成
		orders.setStatus(Orders.COMPLETED);
		orders.setDeliveryTime(LocalDateTime.now());
		orderMapper.update(orders);
	}

	/**
	 * @param address
	 */
	@Override
	public void checkOutOfRange(String address) {
		Map map = new HashMap();
		map.put("address",shopAddress);
		map.put("output","json");
		map.put("ak",ak);

		//获取店铺的经纬度坐标
		String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

		JSONObject jsonObject = JSON.parseObject(shopCoordinate);
		if(!jsonObject.getString("status").equals("0")){
			throw new OrderBusinessException("店铺地址解析失败");
		}

		//数据解析
		JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
		String lat = location.getString("lat");
		String lng = location.getString("lng");
		//店铺经纬度坐标
		String shopLngLat = lat + "," + lng;

		map.put("address",address);
		//获取用户收货地址的经纬度坐标
		String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

		jsonObject = JSON.parseObject(userCoordinate);
		if(!jsonObject.getString("status").equals("0")){
			throw new OrderBusinessException("收货地址解析失败");
		}

		//数据解析
		location = jsonObject.getJSONObject("result").getJSONObject("location");
		lat = location.getString("lat");
		lng = location.getString("lng");
		//用户收货地址经纬度坐标
		String userLngLat = lat + "," + lng;

		map.put("origin",shopLngLat);
		map.put("destination",userLngLat);
		map.put("steps_info","0");

		//路线规划
		String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

		jsonObject = JSON.parseObject(json);
		if(!jsonObject.getString("status").equals("0")){
			throw new OrderBusinessException("配送路线规划失败");
		}
		//数据解析
		JSONObject result = jsonObject.getJSONObject("result");
		JSONArray jsonArray = (JSONArray) result.get("routes");
		Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

		if(distance > 5000){
			//配送距离超过5000米
			throw new OrderBusinessException("超出配送范围");
		}
	}

	/**
	 * 用户催单
	 *
	 * @param id
	 */
	@Override
	public void reminder(Long id) {
		// 根据id查询订单
		Orders ordersDB = orderMapper.getById(id);

		// 校验订单是否存在
		if (ordersDB == null) {
			throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
		}
		Map map = new HashMap();
		map.put("type", 2);	// 1 来单提醒 2 客户催单
		map.put("orderId", id);
		map.put("content", "订单号:" + ordersDB.getNumber());
		webSocketServer.sendToAllClient(JSON.toJSONString(map));
	}
}
