package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 数据统计相关接口
 */
@Api(tags = "数据统计相关接口")
@Slf4j
@RestController
@RequestMapping("/admin/report/")
public class ReportController {
	@Autowired
	private ReportService reportService;
	/**
	 * 营业额统计接口
	 * @param begin
	 * @param end
	 * @return
	 */
	@ApiOperation("营业额统计接口")
	@GetMapping("/turnoverStatistics")
	public Result<TurnoverReportVO> turnoverStatistics(
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
		log.info("营业额数据统计:{},{}", begin, end);
		TurnoverReportVO turnoverStatistics = reportService.getTurnoverStatistics(begin, end);
		return Result.success(turnoverStatistics);
	}

	/**
	 *  用户他 统计
	 * @param begin
	 * @param end
	 * @return
	 */
	@ApiOperation("用户统计接口")
	@GetMapping("/userStatistics")
	public Result<UserReportVO> userStatistics(
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

		log.info("用户统计：{}, {}", begin, end);
		UserReportVO userReportVO = reportService.getUserStatistics(begin, end);
		return Result.success(userReportVO);
	}

	/**
	 *  订单统计
	 * @param begin
	 * @param end
	 * @return
	 */
	@ApiOperation("订单统计接口")
	@GetMapping("/ordersStatistics")
	public Result<OrderReportVO> ordersStatistics(
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
		log.info("订单统计:{}, {}",begin, end);
		OrderReportVO orderStatisticsVO = reportService.getOrdersStatistics(begin, end);
		return Result.success(orderStatisticsVO);
	}

	/**
	 *  查询销量排名top10
	 * @param begin
	 * @param end
	 * @return
	 */
	@ApiOperation("查询销量排名top10接口")
	@GetMapping("/top10")
	public Result<SalesTop10ReportVO> top10(
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
		log.info("查询销量排名top10,{},{}", begin, end);
		SalesTop10ReportVO salesTop10ReportVO = reportService.getSaleTop10(begin, end);
		return Result.success(salesTop10ReportVO);
	}
}
