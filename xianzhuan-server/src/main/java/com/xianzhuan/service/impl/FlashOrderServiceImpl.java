package com.xianzhuan.service.impl;

import com.xianzhuan.dto.FlashMessage;
import com.xianzhuan.entity.FlashOrder;
import com.xianzhuan.service.FlashOrderService;
import com.xianzhuan.mapper.FlashOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Service
public class FlashOrderServiceImpl implements FlashOrderService {

    @Autowired
    private FlashOrderMapper flashOrderMapper;

    @Override
    public int createOrder(FlashMessage flashMessage) {
        // 步骤1：构建限时优惠订单
        FlashOrder flashOrder = FlashOrder.builder()
                .userId(flashMessage.getUserId())
                .dealId(flashMessage.getDealId())
                .orderTime(LocalDateTime.now())
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        // 步骤2：保存订单
        return flashOrderMapper.save(flashOrder);
    }
}