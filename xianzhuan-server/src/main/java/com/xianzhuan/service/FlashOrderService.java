package com.xianzhuan.service;

import com.xianzhuan.dto.FlashMessage;
import com.xianzhuan.entity.FlashOrder;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface FlashOrderService {
    int createOrder(FlashMessage flashMessage);
}