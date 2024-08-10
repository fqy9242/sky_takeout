package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@Slf4j
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
public class OrderController {
	@Autowired
	private OrderService orderService;
	/**
	 *  用户下单
	 * @param ordersSubmitDTO
	 * @return
	 */
	@ApiOperation("用户下单")
	@PostMapping("/submit")
	public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
		log.info("用户下单:{}", ordersSubmitDTO);
		OrderSubmitVO orderSubmitVo = orderService.submitOrder(ordersSubmitDTO);
		return Result.success(orderSubmitVo);
	}
	/**
	 * 订单支付
	 *
	 * @param ordersPaymentDTO
	 * @return
	 */
	@PutMapping("/payment")
	@ApiOperation("订单支付")
	public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
		log.info("订单支付：{}", ordersPaymentDTO);
		OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
		log.info("生成预支付交易单：{}", orderPaymentVO);
		return Result.success(orderPaymentVO);
	}
	/**
	 *  历史订单查询
	 */
	@GetMapping("/historyOrders")
	@ApiOperation("历史订单查询")
	public Result<PageResult> list(OrdersPageQueryDTO ordersPageQueryDTO) {
		log.info("查询历史订单:{}", ordersPageQueryDTO);
		PageResult pageResult = orderService.page(ordersPageQueryDTO);
		return Result.success(pageResult);
	}

	/**
	 *  查询订单详情
	 * @return
	 */
	@ApiOperation("查询订单详情")
	@GetMapping("orderDetail/{id}")
	public Result<OrderVO> orderDetail(@PathVariable Long id) {
		log.info("查询订单详情:{}", id);
		OrderVO orderVO = orderService.detail(id);
		return Result.success(orderVO);

	}

	/**
	 *  取消订单
	 * @return
	 */
	@ApiOperation("取消订单")
	@PutMapping("/cancel/{id}")
	public Result cancel(@PathVariable Long id) {
		log.info("取消订单:{}", id);
		orderService.cancelOrder(id);
		return Result.success();
	}

	/**
	 *  再来一单
	 * @param id
	 * @return
	 */
	@ApiOperation("再来一单")
	@PostMapping("/repetition/{id}")
	public Result repetition(@PathVariable Long id) {
		log.info("用户再来一单:{}", id);
		orderService.repetition(id);
		return Result.success();
	}

	/**
	 *  催单
	 */
	@ApiOperation("用户催单")
	@GetMapping("reminder/{id}")
	public Result reminder(@PathVariable Long id) {
		log.info("用户催单:{}", id);
		orderService.reminder(id);
		return Result.success();
	}
}
