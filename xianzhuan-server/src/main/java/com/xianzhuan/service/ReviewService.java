package com.xianzhuan.service;

import com.xianzhuan.dto.ItemReviewDTO;
import com.xianzhuan.entity.ItemReview;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface ReviewService {

    /**
     * 提交商品评价
     *
     * @param itemReviewDTO 评价信息
     */
    void submitReview(ItemReviewDTO itemReviewDTO);

    /**
     * 根据商品ID查询评价列表
     *
     * @param itemId 商品ID
     * @return 评价列表
     */
    List<ItemReview> getByItemId(Long itemId);

    /**
     * 根据卖家ID查询评价列表
     *
     * @param sellerId 卖家ID
     * @return 评价列表
     */
    List<ItemReview> getBySellerId(Long sellerId);
}
