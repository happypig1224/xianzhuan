package com.xianzhuan.controller.admin;

import com.xianzhuan.result.Result;
import com.xianzhuan.service.WorkspaceService;
import com.xianzhuan.vo.BusinessDataVO;
import com.xianzhuan.vo.ItemOverViewVO;
import com.xianzhuan.vo.OrderOverViewVO;
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
@DisplayName("WorkSpaceController 单元测试")
class WorkSpaceControllerTest {

    @Mock
    private WorkspaceService workspaceService;

    @InjectMocks
    private WorkSpaceController workSpaceController;

    @Test
    @DisplayName("工作台今日数据查询 - 成功")
    void businessData_success() {
        // 1. 准备数据
        BusinessDataVO businessDataVO = new BusinessDataVO();
        when(workspaceService.getBusinessData(any(), any())).thenReturn(businessDataVO);

        // 2. 调用方法
        Result<BusinessDataVO> result = workSpaceController.businessData();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(workspaceService).getBusinessData(any(), any());
    }

    @Test
    @DisplayName("查询订单管理数据 - 成功")
    void orderOverView_success() {
        // 1. 准备数据
        OrderOverViewVO orderOverViewVO = new OrderOverViewVO();
        when(workspaceService.getOrderOverView()).thenReturn(orderOverViewVO);

        // 2. 调用方法
        Result<OrderOverViewVO> result = workSpaceController.orderOverView();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(workspaceService).getOrderOverView();
    }

    @Test
    @DisplayName("查询物品总览 - 成功")
    void itemOverView_success() {
        // 1. 准备数据
        ItemOverViewVO itemOverViewVO = new ItemOverViewVO();
        when(workspaceService.getItemOverView()).thenReturn(itemOverViewVO);

        // 2. 调用方法
        Result<ItemOverViewVO> result = workSpaceController.itemOverView();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(workspaceService).getItemOverView();
    }
}