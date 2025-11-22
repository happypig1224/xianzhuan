package com.xianzhuan.dto;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public record QueryOrdersRequest(
        Integer status,
        Integer page,
        Integer pageSize
) {}
