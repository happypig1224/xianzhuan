package com.xianzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品评价
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemReview implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //商品ID
    private Long itemId;

    //用户ID
    private Long userId;

    //订单ID
    private Long orderId;

    //评分1-5
    private Integer rating;

    //评价内容
    private String content;

    //评价图片URL,逗号分隔
    private String images;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;
}