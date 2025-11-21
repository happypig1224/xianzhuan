package com.xianzhuan.controller.admin;

import com.xianzhuan.entity.FlashDeal;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.FlashDealService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("管理端 FlashDealController 单元测试")
class FlashDealControllerTest {

    @Mock
    private FlashDealService flashDealService;

    @InjectMocks
    private FlashDealController flashDealController;

    @Test
    @DisplayName("发布限时优惠 - 成功")
    void publishDeal_success() {
        // 1. 准备数据
        FlashDeal flashDeal = new FlashDeal();
        when(flashDealService.publishDeal(any(FlashDeal.class))).thenReturn(Result.success("发布成功"));

        // 2. 调用方法
        Result<String> result = flashDealController.publishDeal(flashDeal);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertEquals("发布成功", result.getData());
        verify(flashDealService).publishDeal(any(FlashDeal.class));
    }
}