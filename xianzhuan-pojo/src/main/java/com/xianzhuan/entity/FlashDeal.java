package com.xianzhuan.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 闪购商品
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashDeal {

    private Long id;

    //闪购商品ID
    private Long voucherId;

    //标题
    private String title;

    //使用规则
    private String rules;

    //支付金额，单位是分
    private Long payValue;

    //抵扣金额，单位是分
    private Long actualValue;

    //1上架 2下架 3过期
    private Integer status;

    //库存
    private Integer stock;

    //生效时间
    private LocalDateTime beginTime;

    //失效时间
    private LocalDateTime endTime;

    //创建时间
    private LocalDateTime createTime;
}