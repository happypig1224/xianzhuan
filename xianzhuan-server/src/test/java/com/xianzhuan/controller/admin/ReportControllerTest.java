package com.xianzhuan.controller.admin;

import com.xianzhuan.result.Result;
import com.xianzhuan.service.ReportService;
import com.xianzhuan.vo.OrderReportVO;
import com.xianzhuan.vo.SalesTop10ReportVO;
import com.xianzhuan.vo.TurnoverReportVO;
import com.xianzhuan.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController 单元测试")
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private LocalDate begin;
    private LocalDate end;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        begin = LocalDate.of(2026, 6, 1);
        end = LocalDate.of(2026, 6, 7);
    }

    @Test
    @DisplayName("营业额数据统计 - 成功")
    void turnoverStatistics_success() {
        // 1. 准备数据
        TurnoverReportVO reportVO = new TurnoverReportVO();
        when(reportService.getTurnOver(any(), any())).thenReturn(reportVO);

        // 2. 调用方法
        Result<TurnoverReportVO> result = reportController.report(begin, end);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(reportService).getTurnOver(begin, end);
    }

    @Test
    @DisplayName("用户数据统计 - 成功")
    void userStatistics_success() {
        // 1. 准备数据
        UserReportVO reportVO = new UserReportVO();
        when(reportService.getUserStatistics(any(), any())).thenReturn(reportVO);

        // 2. 调用方法
        Result<UserReportVO> result = reportController.userStatistics(begin, end);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(reportService).getUserStatistics(begin, end);
    }

    @Test
    @DisplayName("订单数据统计 - 成功")
    void orderStatistics_success() {
        // 1. 准备数据
        OrderReportVO reportVO = new OrderReportVO();
        when(reportService.getOrderStatistics(any(), any())).thenReturn(reportVO);

        // 2. 调用方法
        Result<OrderReportVO> result = reportController.orderStatistics(begin, end);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(reportService).getOrderStatistics(begin, end);
    }

    @Test
    @DisplayName("销量排名统计 - 成功")
    void top10_success() {
        // 1. 准备数据
        SalesTop10ReportVO reportVO = new SalesTop10ReportVO();
        when(reportService.getSalesTop10(any(), any())).thenReturn(reportVO);

        // 2. 调用方法
        Result<SalesTop10ReportVO> result = reportController.top10(begin, end);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(reportService).getSalesTop10(begin, end);
    }

    @Test
    @DisplayName("导出运营数据报表 - 成功")
    void export_success() {
        // 1. 准备数据
        HttpServletResponse response = mock(HttpServletResponse.class);
        doNothing().when(reportService).exportBusinessData(any(HttpServletResponse.class));

        // 2. 调用方法
        reportController.export(response);

        // 3. 验证结果
        verify(reportService).exportBusinessData(any(HttpServletResponse.class));
    }
}