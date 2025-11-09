package com.xianzhuan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
public class OrdersSubmitDTO implements Serializable {
    //地址id
    private Long addressId;
    //付款方式
    private int payMethod;
    //备注
    private String remark;
    //预计送达时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;
    //配送状态  1立即送出  0选择具体时间
    private Integer shippingStatus;
    //总金额
    private BigDecimal amount;
    //订单提交Token（前端从GET /user/order/token获取）
    private String token;
}
