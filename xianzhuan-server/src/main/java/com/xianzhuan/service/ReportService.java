package com.xianzhuan.service;

import com.xianzhuan.vo.OrderReportVO;
import com.xianzhuan.vo.SalesTop10ReportVO;
import com.xianzhuan.vo.TurnoverReportVO;
import com.xianzhuan.vo.UserReportVO;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    TurnoverReportVO getTurnOver(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);
    /**
     * 导出近30天的运营数据报表
     * @param response
     **/
    void exportBusinessData(HttpServletResponse response);
}
