package com.sky.service;

import com.sky.vo.*;

import java.time.LocalDate;

public interface ReportService {
	/**
	 * 营业额统计
	 * @return
	 */
	TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

	/**
	 *  用户统计
	 * @param begin
	 * @param end
	 * @return
	 */
	UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

	/**
	 *  获取指定日期区间内的订单数据
	 * @param begin
	 * @param end
	 * @return
	 */
	OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

	/**
	 * 查询销量排名top10
	 * @param begin
	 * @param end
	 * @return
	 */
	SalesTop10ReportVO getSaleTop10(LocalDate begin, LocalDate end);
}
