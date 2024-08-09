package com.sky.vo;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO extends Orders implements Serializable {
    // 订单菜品信息
    private String orderDishes;
    // 地址
    private String address;
    // 地址簿id
    private Long addressBookId;
    //实收金额
    private BigDecimal amount;
    //订单取消原因
    private String cancelReason;
    //订单取消时间
    private LocalDateTime cancelTime;
    //结账时间
    private LocalDateTime checkoutTime;
    //收货人
    private String consignee;
    //配送状态  1立即送出  0选择具体时间
    private Integer deliveryStatus;
    //送达时间
    private LocalDateTime deliveryTime;
    //预计送达时间
    private LocalDateTime estimatedDeliveryTime;
    //订单号
    private String number;
    //下单时间
    private LocalDateTime orderTime;
    //打包费
    private int packAmount;
    //支付方式 1微信，2支付宝
    private Integer payMethod;
    //支付状态 0未支付 1已支付 2退款
    private Integer payStatus;
    //手机号
    private String phone;
    //订单拒绝原因
    private String rejectionReason;
    //备注
    private String remark;
    //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款
    private Integer status;
    //餐具数量
    private int tablewareNumber;
    //餐具数量状态  1按餐量提供  0选择具体数量
    private Integer tablewareStatus;
    //下单用户id
    private Long userId;
    //用户名
    private String userName;
    //订单详情
    private List<OrderDetail> orderDetailList;

}
