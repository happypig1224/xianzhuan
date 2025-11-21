package com.xianzhuan.controller.admin;

import com.xianzhuan.dto.*;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.OrderService;
import com.xianzhuan.vo.OrderStatisticsVO;
import com.xianzhuan.vo.OrderVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("管理端 OrderController 单元测试")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    @DisplayName("订单搜索 - 成功")
    void conditionSearch_success() {
        // 1. 准备数据
        OrdersPageQueryDTO queryDTO = new OrdersPageQueryDTO();
        PageResult pageResult = new PageResult(10L, new ArrayList<>());
        when(orderService.OrdersSearch(any(OrdersPageQueryDTO.class))).thenReturn(pageResult);

        // 2. 调用方法
        Result<PageResult> result = orderController.conditionSearch(queryDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(orderService).OrdersSearch(any(OrdersPageQueryDTO.class));
    }

    @Test
    @DisplayName("各个状态的订单数量统计 - 成功")
    void orderStatistics_success() {
        // 1. 准备数据
        OrderStatisticsVO statisticsVO = new OrderStatisticsVO();
        when(orderService.getOrderStatistics()).thenReturn(statisticsVO);

        // 2. 调用方法
        Result<OrderStatisticsVO> result = orderController.orderStatistics();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(orderService).getOrderStatistics();
    }

    @Test
    @DisplayName("订单详情 - 成功")
    void orderDetail_success() {
        // 1. 准备数据
        OrderVO orderVO = new OrderVO();
        when(orderService.getDetailByOrderId(1L)).thenReturn(orderVO);

        // 2. 调用方法
        Result<OrderVO> result = orderController.orderDetail(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(orderService).getDetailByOrderId(1L);
    }

    @Test
    @DisplayName("接单 - 成功")
    void confirm_success() {
        // 1. 准备数据
        OrdersConfirmDTO confirmDTO = new OrdersConfirmDTO();
        doNothing().when(orderService).confirm(any(OrdersConfirmDTO.class));

        // 2. 调用方法
        Result result = orderController.confirm(confirmDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(orderService).confirm(any(OrdersConfirmDTO.class));
    }

    @Test
    @DisplayName("拒单 - 成功")
    void rejection_success() {
        // 1. 准备数据
        OrdersRejectionDTO rejectionDTO = new OrdersRejectionDTO();
        doNothing().when(orderService).rejection(any(OrdersRejectionDTO.class));

        // 2. 调用方法
        Result result = orderController.rejection(rejectionDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(orderService).rejection(any(OrdersRejectionDTO.class));
    }

    @Test
    @DisplayName("取消订单 - 成功")
    void cancelOrder_success() {
        // 1. 准备数据
        OrdersCancelDTO cancelDTO = new OrdersCancelDTO();
        doNothing().when(orderService).cancelOrderAdmin(any(OrdersCancelDTO.class));

        // 2. 调用方法
        Result result = orderController.cancelOrder(cancelDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(orderService).cancelOrderAdmin(any(OrdersCancelDTO.class));
    }

    @Test
    @DisplayName("派送订单 - 成功")
    void ship_success() {
        // 1. 准备数据
        doNothing().when(orderService).ship(1L);

        // 2. 调用方法
        Result result = orderController.ship(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(orderService).ship(1L);
    }

    @Test
    @DisplayName("完成订单 - 成功")
    void complete_success() {
        // 1. 准备数据
        doNothing().when(orderService).complete(1L);

        // 2. 调用方法
        Result result = orderController.complete(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(orderService).complete(1L);
    }
}