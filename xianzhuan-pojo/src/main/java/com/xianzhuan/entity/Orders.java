package com.xianzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders implements Serializable {

    /**
     * 订单状态 1待付款 2待发货 3已发货 4待收货 5售后中 6交易完成 7交易关闭
     */
    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_SHIPPED = 2;
    public static final Integer SHIPPED = 3;
    public static final Integer TO_BE_RECEIVED = 4;
    public static final Integer AFTER_SALE = 5;
    public static final Integer COMPLETED = 6;
    public static final Integer CANCELLED = 7;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private static final long serialVersionUID = 1L;

    private Long id;

    //订单号
    private String number;

    //订单状态 1待付款 2待发货 3已发货 4待收货 5售后中 6交易完成 7交易关闭
    private Integer status;

    //下单用户id
    private Long userId;

    //卖家ID
    private Long sellerId;

    //地址id
    private Long addressId;

    //下单时间
    private LocalDateTime orderTime;

    //结账时间
    private LocalDateTime checkoutTime;

    //支付方式 1微信，2支付宝
    private Integer payMethod;

    //支付状态 0未支付 1已支付 2退款
    private Integer payStatus;

    //实收金额
    private BigDecimal amount;

    //备注
    private String remark;

    //用户名
    private String userName;

    //手机号
    private String phone;

    //地址
    private String address;

    //收货人
    private String consignee;

    //订单取消原因
    private String cancelReason;

    //订单拒绝原因
    private String rejectionReason;

    //订单取消时间
    private LocalDateTime cancelTime;

    //预计送达时间
    private LocalDateTime estimatedDeliveryTime;

    //配送状态 1立即送出 0选择具体时间
    private Integer shippingStatus;

    //送达时间
    private LocalDateTime shippingTime;

    //乐观锁
    private Integer version;
}
