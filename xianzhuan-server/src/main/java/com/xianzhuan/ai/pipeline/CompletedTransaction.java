package com.xianzhuan.ai.pipeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成交记录（离线管线查询结果）
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletedTransaction {
    private Long itemId; // 商品ID
    private String itemName; // 商品名称
    private Long categoryId; // 商品类目ID
    private String categoryName; // 商品类目名称
    private BigDecimal originalPrice; // 商品原价，单位：元
    private BigDecimal transactionPrice; // 成交价格，单位：元
    private String condition; // 商品成色描述，示例："全新"、"九成新"、"八成新"、"七成新"、"六成新"、"五成新"、"四成新"、"三成新"、"二成新"、"一成新"、"零成新"、"九成旧"、"八成旧"、"七成旧"、"六成旧"、"五成旧"、"四成旧"、"三成旧"、"二成旧"、"一成旧"、"零成旧"、"九成
    private BigDecimal discountRate; // 折扣率，如9.5表示9.5折
    private LocalDateTime checkoutTime; // 成交时间
}