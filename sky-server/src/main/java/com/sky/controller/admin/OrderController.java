package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Api(tags = "管理端订单相关接口")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {
	@Autowired
	private OrderService orderService;
	/**
	 *  订单搜索
	 * @return
	 */
	@ApiOperation("订单搜索")
	@GetMapping("/conditionSearch")
	public Result<PageResult> list(OrdersPageQueryDTO ordersPageQueryDTO) {
		log.info("订单搜索:{}", ordersPageQueryDTO);
		PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
		return Result.success(pageResult);
	}

	/**
	 * 各个状态的订单数量统计
	 * @return
	 */
	@ApiOperation("各个状态的订单数量统计")
	@GetMapping("statistics")
	public Result<OrderStatisticsVO> statistics() {
		log.info("各个状态的订单数量统计");
		OrderStatisticsVO orderStatisticsVO = orderService.statistics();
		return Result.success(orderStatisticsVO);
	}
	/**
	 *  查询订单详情
	 * @return
	 */
	@ApiOperation("查询订单详情")
	@GetMapping("details/{id}")
	public Result<OrderVO> orderDetail(@PathVariable Long id) {
		log.info("管理端查询订单详情:{}", id);
		OrderVO orderVO = orderService.detail(id);
		return Result.success(orderVO);
	}

	/**
	 *  接单
	 * @param ordersConfirmDTO
	 * @return
	 */
	@ApiOperation("接单")
	@PutMapping("/confirm")
	public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
		log.info("管理端接单:{}", ordersConfirmDTO);
		orderService.confirm(ordersConfirmDTO);
		return Result.success();
	}
	/**
	 * 拒单
	 *
	 * @return
	 */
	@PutMapping("/rejection")
	@ApiOperation("拒单")
	public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
		orderService.rejection(ordersRejectionDTO);
		return Result.success();
	}
	@PutMapping("/cancel")
	public Result cancel(OrdersCancelDTO ordersCancelDTO) {
		log.info("管理端取消订单:{}", ordersCancelDTO);
		orderService.cancel(ordersCancelDTO);
		return Result.success();
	}
	/**
	 * 派送订单
	 *
	 * @return
	 */
	@PutMapping("/delivery/{id}")
	@ApiOperation("派送订单")
	public Result delivery(@PathVariable("id") Long id) {
		orderService.delivery(id);
		return Result.success();
	}
	/**
	 * 完成订单
	 *
	 * @return
	 */
	@PutMapping("/complete/{id}")
	@ApiOperation("完成订单")
	public Result complete(@PathVariable("id") Long id) {
		orderService.complete(id);
		return Result.success();
	}

}
