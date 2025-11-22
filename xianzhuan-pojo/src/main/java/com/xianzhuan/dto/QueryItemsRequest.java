package com.xianzhuan.dto;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public record QueryItemsRequest(
        Long categoryId,
        String spec,
        Integer maxPrice,
        String keyword
) {}