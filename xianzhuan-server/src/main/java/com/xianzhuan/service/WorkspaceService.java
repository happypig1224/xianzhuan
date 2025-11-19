package com.xianzhuan.service;

import com.xianzhuan.vo.BusinessDataVO;
import com.xianzhuan.vo.ItemOverViewVO;
import com.xianzhuan.vo.OrderOverViewVO;
import java.time.LocalDateTime;

public interface WorkspaceService {

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询订单管理数据
     * @return
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 查询商品总览
     * @return
     */
    ItemOverViewVO getItemOverView();

}
