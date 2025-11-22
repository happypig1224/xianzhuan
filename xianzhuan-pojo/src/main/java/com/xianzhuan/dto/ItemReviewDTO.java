package com.xianzhuan.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
public class ItemReviewDTO implements Serializable {

    private Long itemId;

    private Long orderId;

    private Integer rating;

    private String content;

    private String images;
}