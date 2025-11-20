package com.xianzhuan.mapper;

import com.xianzhuan.ai.pipeline.CompletedTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 智能定价数据查询Mapper（仅离线管线使用，在线估价不调用）
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Mapper
public interface PricingDataMapper {

    /**
     * 查询所有已完成订单的成交记录
     * 仅供离线管线使用，在线估价不调用此方法
     */
    @Select("""
            SELECT
                ui.id AS itemId,
                ui.name AS itemName,
                ui.category_id AS categoryId,
                c.name AS categoryName,
                ui.original_price AS originalPrice,
                od.amount AS transactionPrice,
                ui.`condition` AS `condition`,
                ROUND(od.amount / ui.original_price, 4) AS discountRate,
                o.checkout_time AS checkoutTime
            FROM order_detail od
            JOIN orders o ON od.order_id = o.id
            JOIN used_item ui ON od.item_id = ui.id
            JOIN category c ON ui.category_id = c.id
            WHERE o.status = 6
              AND ui.original_price > 0
            ORDER BY o.checkout_time DESC
            """)
    List<CompletedTransaction> queryAllCompletedTransactions();
}