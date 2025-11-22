package com.xianzhuan.dto;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public record AddToCartRequest(
        Long dishId,

        Integer number,
        String dishFlavor
) {}
