package com.xianzhuan.service.impl;

import com.xianzhuan.context.BaseContext;
import com.xianzhuan.dto.ItemReviewDTO;
import com.xianzhuan.entity.ItemReview;
import com.xianzhuan.mapper.ReviewMapper;
import com.xianzhuan.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public void submitReview(ItemReviewDTO itemReviewDTO) {
        // 步骤1：构建评价实体
        ItemReview itemReview = new ItemReview();
        BeanUtils.copyProperties(itemReviewDTO, itemReview);
        itemReview.setUserId(BaseContext.getCurrentId());
        itemReview.setCreateTime(LocalDateTime.now());
        // 步骤2：保存评价
        reviewMapper.insert(itemReview);
        log.info("用户{}提交商品{}评价成功", itemReview.getUserId(), itemReview.getItemId());
    }

    @Override
    public List<ItemReview> getByItemId(Long itemId) {
        // 步骤1：根据商品ID查询评价列表
        return reviewMapper.listByItemId(itemId);
    }

    @Override
    public List<ItemReview> getBySellerId(Long sellerId) {
        // 步骤1：根据卖家ID查询评价列表
        return reviewMapper.listBySellerId(sellerId);
    }
}
