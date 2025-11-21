package com.xianzhuan.controller.user;

import com.xianzhuan.dto.OrdersPaymentDTO;
import com.xianzhuan.dto.OrdersSubmitDTO;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.OrderService;
import com.xianzhuan.vo.OrderPaymentVO;
import com.xianzhuan.vo.OrderSubmitVO;
import com.xianzhuan.vo.OrderVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户端 OrderController 单元测试")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    @DisplayName("获取订单提交Token - 成功")
    void getToken_success() {
        // 1. 准备数据
        when(orderService.generateToken()).thenReturn("orderToken123");

        // 2. 调用方法
        Result<String> result = orderController.getToken();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertEquals("orderToken123", result.getData());
        verify(orderService).generateToken();
    }

    @Test
    @DisplayName("用户下单 - 成功")
    void submit_success() {
        // 1. 准备数据
        OrdersSubmitDTO submitDTO = new OrdersSubmitDTO();
        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .id(1L)
                .orderNumber("ORD202606070001")
                .build();
        when(orderService.submitOrder(any(OrdersSubmitDTO.class))).thenReturn(submitVO);

        // 2. 调用方法
        Result<OrderSubmitVO> result = orderController.submit(submitDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
        verify(orderService).submitOrder(any(OrdersSubmitDTO.class));
    }

    @Test
    @DisplayName("订单支付 - 成功")
    void payment_success() throws Exception {
        // 1. 准备数据
        OrdersPaymentDTO paymentDTO = new OrdersPaymentDTO();
        OrderPaymentVO paymentVO = new OrderPaymentVO();
        when(orderService.payment(any(OrdersPaymentDTO.class))).thenReturn(paymentVO);

        // 2. 调用方法
        Result<OrderPaymentVO> result = orderController.payment(paymentDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(orderService).payment(any(OrdersPaymentDTO.class));
    }

    @Test
    @DisplayName("历史订单查询 - 成功")
    void page_success() {
        // 1. 准备数据
        PageResult pageResult = new PageResult(10L, new ArrayList<>());
        when(orderService.pageQueryUser(anyInt(), anyInt(), any())).thenReturn(pageResult);

        // 2. 调用方法
        Result<PageResult> result = orderController.page(1, 10, 1);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(orderService).pageQueryUser(1, 10, 1);
    }

    @Test
    @DisplayName("订单详情 - 成功")
    void detailsOrder_success() {
        // 1. 准备数据
        OrderVO orderVO = new OrderVO();
        when(orderService.getDetailByOrderId(1L)).thenReturn(orderVO);

        // 2. 调用方法
        Result<OrderVO> result = orderController.detailsOrder(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(orderService).getDetailByOrderId(1L);
    }

    @Test
    @DisplayName("取消订单 - 成功")
    void cancelOrder_success() {
        // 1. 准备数据
        doNothing().when(orderService).cancelOrder(1L);

        // 2. 调用方法
        Result result = orderController.cancelOrder(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(orderService).cancelOrder(1L);
    }

    @Test
    @DisplayName("再来一单 - 成功")
    void repurchase_success() {
        // 1. 准备数据
        doNothing().when(orderService).repurchase(1L);

        // 2. 调用方法
        Result result = orderController.repurchase(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(orderService).repurchase(1L);
    }

    @Test
    @DisplayName("用户催单 - 成功")
    void reminder_success() {
        // 1. 准备数据
        doNothing().when(orderService).reminder(1L);

        // 2. 调用方法
        Result result = orderController.reminder(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(orderService).reminder(1L);
    }
}