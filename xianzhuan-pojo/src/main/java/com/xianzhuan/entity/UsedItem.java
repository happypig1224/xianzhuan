package com.xianzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二手商品
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //商品名称
    private String name;

    //商品分类id
    private Long categoryId;

    //品牌
    private String brand;

    //原价
    private BigDecimal originalPrice;

    //现价
    private BigDecimal currentPrice;

    //运费
    private BigDecimal shippingFee;

    //成色
    private String condition;

    //卖家ID
    private Long sellerId;

    //浏览量
    private Integer views;

    //图片
    private String image;

    //描述信息
    private String description;

    //销量
    private Integer salesCount;

    //0 停售 1 起售
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;

}