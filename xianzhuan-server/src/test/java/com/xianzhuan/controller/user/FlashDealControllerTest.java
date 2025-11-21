package com.xianzhuan.controller.user;

import com.xianzhuan.entity.FlashDeal;
import com.xianzhuan.entity.FlashOrder;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.FlashDealService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户端 FlashDealController 单元测试")
class FlashDealControllerTest {

    @Mock
    private FlashDealService flashDealService;

    @InjectMocks
    private FlashDealController flashDealController;

    @Test
    @DisplayName("限时抢购 - 成功")
    void flashBuy_success() {
        // 1. 准备数据
        Result<String> expectedResult = Result.success("抢购成功");
        when(flashDealService.flashBuy(1L)).thenReturn(expectedResult);

        // 2. 调用方法
        Result<String> result = flashDealController.flashBuy(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertEquals("抢购成功", result.getData());
        verify(flashDealService).flashBuy(1L);
    }

    @Test
    @DisplayName("查询限时优惠 - 成功")
    void queryDeal_success() {
        // 1. 准备数据
        Result<List<FlashDeal>> expectedResult = Result.success(new ArrayList<>());
        when(flashDealService.queryDeal()).thenReturn(expectedResult);

        // 2. 调用方法
        Result<List<FlashDeal>> result = flashDealController.queryDeal();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(flashDealService).queryDeal();
    }

    @Test
    @DisplayName("查询用户优惠列表 - 成功")
    void myVoucherList_success() {
        // 1. 准备数据
        Result<List<FlashOrder>> expectedResult = Result.success(new ArrayList<>());
        when(flashDealService.myDealList()).thenReturn(expectedResult);

        // 2. 调用方法
        Result<List<FlashOrder>> result = flashDealController.myVoucherList();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(flashDealService).myDealList();
    }
}