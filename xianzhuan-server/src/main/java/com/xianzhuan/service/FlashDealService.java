package com.xianzhuan.service;

import com.xianzhuan.entity.FlashOrder;
import com.xianzhuan.entity.FlashDeal;
import com.xianzhuan.result.Result;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface FlashDealService {
    // 限时优惠发布
    Result<String> publishDeal(FlashDeal flashDeal);

    Result<String> flashBuy(Long dealId);

    Result<List<FlashDeal>> queryDeal();

    Result<List<FlashOrder>> myDealList();

}