package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private WorkspaceService workspaceService;
	/**
	 * 营业额统计
	 *
	 * @param begin
	 * @param end
	 * @return
	 */
	@Override
	public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
		// 用于存放每天营业额的列表
		List<Double> turnoverList = new ArrayList<>();
		// 范围内的日期
		List<LocalDate> dateList = getDateListBeginToEnd(begin, end);
		for (LocalDate date : dateList) {
			// 查询date的营业额 也就是已完成订单的总金额
			LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
			LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
			Map map = new HashMap();
			map.put("begin", beginTime);
			map.put("end", endTime);
			map.put("status", Orders.COMPLETED);
			Double turnover = orderMapper.sumByMap(map);
			turnover = turnover == null ? 0.0: turnover;
			turnoverList.add( turnover);
		}

		return TurnoverReportVO
				.builder()
				.dateList(StringUtils.join(dateList, ","))
				.turnoverList(StringUtils.join(turnoverList, ","))
				.build();
	}

	/**
	 * 用户统计
	 *
	 * @param begin
	 * @param end
	 * @return
	 */
	@Override
	public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
		// 获取日期区间列表
		List<LocalDate> dateList = getDateListBeginToEnd(begin, end);
		// 存放截止某天的用户数量
		List<Integer> userCountList = new ArrayList<>();
		// 存放某天新增的用户数量
		List<Integer> newCreateUserCountList = new ArrayList<>();
		for (LocalDate date : dateList) {
			// 统计date这天的用户量及当日新增用户
			LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);	// 开始时间 0:00:00
			LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);	// 结束时间 当前最后一秒
			Map map = new HashMap();
			map.put("endTime", endTime);
			Integer userCount = userMapper.countByMap(map);	// 截止这一天，注册的总用户数
			map.put("beginTime", beginTime);
			Integer  newCreateUserCount = userMapper.countByMap(map);	// 今日新增用户量
			userCountList.add(userCount);
			newCreateUserCountList.add(newCreateUserCount);
		}
		return UserReportVO
				.builder()
				.dateList(StringUtils.join(dateList, ","))
				.totalUserList(StringUtils.join(userCountList, ","))
				.newUserList(StringUtils.join(newCreateUserCountList, ","))
				.build();
	}

	/**
	 * 获取指定日期区间内的订单数据
	 *
	 * @param begin
	 * @param end
	 * @return
	 */
	@Override
	public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
		// 存放每天的订单总数
		List<Integer> orderCountList = new ArrayList<>();
		// 存放每天的有效订单数
		List<Integer> validOrderCountList = new ArrayList<>();
		// 范围内的日期
		List<LocalDate> dateList = getDateListBeginToEnd(begin, end);
		for (LocalDate date : dateList) {
			LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
			LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
			// 查询每天的订单总数
			Integer orderCount = getOrderCount(beginTime, endTime, null);
			orderCountList.add(orderCount);
			// 查询每天的订单有效总数 (状态为已完成的订单)
			Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
			validOrderCountList.add(validOrderCount);
		}
		// 计算时间区间内的订总数量
		Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
		// 计算时间区间内的有效订单数量
		Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
		// 计算订单完成率
		Double orderCompleteRate = 0.0;
		if (totalOrderCount != 0) {
			orderCompleteRate = (double) (validOrderCount / totalOrderCount);
		}
		return OrderReportVO.builder()
				.dateList(StringUtils.join(dateList, ","))
				.orderCountList(StringUtils.join(orderCountList, ","))
				.validOrderCountList(StringUtils.join(orderCountList, ","))
				.totalOrderCount(totalOrderCount)
				.validOrderCount(validOrderCount)
				.orderCompletionRate(orderCompleteRate)
				.build();
	}

	/**
	 *  指定一个开始时间好结束时间，返回一个时间列表，存放从开始时间到结束时间区间内的日期对象
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @return 区间内的日期列表
	 */
	private List<LocalDate> getDateListBeginToEnd(LocalDate begin, LocalDate end) {
		List<LocalDate> dateList = new ArrayList<>();
		dateList.add(begin);
		while (! begin.equals(end)) {
			begin = begin.plusDays(1);
			dateList.add(begin);
		}
		return dateList;
	}

	/**
	 *  根据条件获取订单数
	 * @param begin
	 * @param end
	 * @param status
	 * @return
	 */
	private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
		Map map = new HashMap();

		map.put("begin", begin);
		map.put("end", end);
		map.put("status", status);
		Integer count = orderMapper.countByMap(map);
		return count;
	}

	/**
	 * 查询销量排名top10
	 *
	 * @param begin
	 * @param end
	 * @return
	 */
	@Override
	public SalesTop10ReportVO getSaleTop10(LocalDate begin, LocalDate end) {
		LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
		LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
		List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
		// 加工成vo对象
		List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
		List<Integer> nums = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
		return SalesTop10ReportVO.builder()
				.nameList(StringUtils.join(names, ","))
				.numberList(StringUtils.join(nums, ","))
				.build();
	}

	/**
	 * 导出最近30天运营数据报表
	 *
	 * @param response
	 */
	@Override
	public void exportBusinessDate(HttpServletResponse response) {
		// 1. 查询数据库 获取营业数据 --最近30天
		LocalDate beginDate = LocalDate.now().minusDays(30);
		LocalDate endDate = LocalDate.now().minusDays(1);
		BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(beginDate, LocalTime.MIN),
				LocalDateTime.of(endDate, LocalTime.MAX));
		// 2. 通过POI将数据写入到excel文件中
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
		try {
			XSSFWorkbook excel = new XSSFWorkbook(inputStream);
			// 获得sheet对象
			XSSFSheet sheet1 = excel.getSheet("Sheet1");
			// 填充日期区间数据
			sheet1.getRow(1).getCell(1).setCellValue("时间:" + beginDate + "至" + endDate);
			// 获得第四行对象
			XSSFRow row3 = sheet1.getRow(3);
			// 填充营业额数据
			row3.getCell(2).setCellValue(businessData.getTurnover());
			// 填充订单完成率
			row3.getCell(4).setCellValue(businessData.getOrderCompletionRate());
			// 填充新增用户数
			row3.getCell(6).setCellValue(businessData.getNewUsers());
			// 获取第五行数据
			XSSFRow row5 = sheet1.getRow(4);
			// 填充有效订单数
			row5.getCell(2).setCellValue(businessData.getValidOrderCount());
			// 填充平均客单价
			row5.getCell(4).setCellValue(businessData.getUnitPrice());
			// 填充明细数据
			for (int i = 0; i < 30; i++) {
				LocalDate date = beginDate.plusDays(i);
				// 查询某一天的数据
				BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN),
						LocalDateTime.of(date, LocalTime.MAX));
				// 获取某一行对象
				XSSFRow row = sheet1.getRow(i + 7);
				row.getCell(1).setCellValue(date.toString());
				row.getCell(2).setCellValue(businessDataVO.getTurnover());
				row.getCell(3).setCellValue(businessDataVO.getValidOrderCount());
				row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
				row.getCell(5).setCellValue(businessDataVO.getUnitPrice());
				row.getCell(6).setCellValue(businessDataVO.getNewUsers());
			}
			// 3. 通过输出流将excel文件下载到客户端浏览器
			ServletOutputStream out = response.getOutputStream();
			excel.write(out);
			// 关闭资源
			out.close();
			excel.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}
}
