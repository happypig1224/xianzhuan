package com.xianzhuan.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 闪购订单
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashOrder {

    private Long id;

    //用户id
    private Long userId;

    //闪购商品ID
    private Long dealId;

    //下单时间
    private LocalDateTime orderTime;

    //1待支付 2已支付 3已取消 4已退款 5支付失败 6已删除
    private Integer status;

    //创建时间
    private LocalDateTime createTime;
}