package com.xianzhuan.controller.user;

import com.xianzhuan.dto.ItemReviewDTO;
import com.xianzhuan.entity.ItemReview;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController 单元测试")
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @Test
    @DisplayName("提交评价 - 成功")
    void submit_success() {
        // 1. 准备数据
        ItemReviewDTO dto = new ItemReviewDTO();
        doNothing().when(reviewService).submitReview(any(ItemReviewDTO.class));

        // 2. 调用方法
        Result result = reviewController.submit(dto);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(reviewService).submitReview(any(ItemReviewDTO.class));
    }

    @Test
    @DisplayName("根据商品id查询评价列表 - 成功")
    void listByItem_success() {
        // 1. 准备数据
        List<ItemReview> reviews = new ArrayList<>();
        when(reviewService.getByItemId(1L)).thenReturn(reviews);

        // 2. 调用方法
        Result<List<ItemReview>> result = reviewController.listByItem(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(reviewService).getByItemId(1L);
    }

    @Test
    @DisplayName("查询当前卖家收到的评价列表 - 成功")
    void listBySeller_success() {
        // 1. 准备数据
        List<ItemReview> reviews = new ArrayList<>();
        when(reviewService.getBySellerId(null)).thenReturn(reviews);

        // 2. 调用方法
        Result<List<ItemReview>> result = reviewController.listBySeller();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(reviewService).getBySellerId(null);
    }
}