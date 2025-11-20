package com.xianzhuan.ai.pipeline;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 估价知识片段（按品牌+品类+成色分组聚合后的结果）
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
public class PricingKnowledgeSegment {
    private String groupKey; // 组合键
    private String brand;
    private Long categoryId;
    private String categoryName;
    private String condition; // 成色
    private BigDecimal avgOriginalPrice;   // 平均原价
    private BigDecimal avgTransactionPrice; // 平均成交价
    private BigDecimal minTransactionPrice; // 最低成交价
    private BigDecimal maxTransactionPrice; // 最高成交价
    private BigDecimal avgDiscountRate; // 平均折价率
    private int transactionCount; // 成交笔数
    private String representativeName; // 代表商品名称

    /**
     * 从第一条成交记录创建知识片段
     */
    public static PricingKnowledgeSegment fromFirst(CompletedTransaction t, String brand) {
        PricingKnowledgeSegment seg = new PricingKnowledgeSegment();
        seg.groupKey = brand + "|" + t.getCategoryId() + "|" + t.getCondition();
        seg.brand = brand;
        seg.categoryId = t.getCategoryId();
        seg.categoryName = t.getCategoryName();
        seg.condition = t.getCondition();
        seg.avgOriginalPrice = t.getOriginalPrice();
        seg.avgTransactionPrice = t.getTransactionPrice();
        seg.minTransactionPrice = t.getTransactionPrice();
        seg.maxTransactionPrice = t.getTransactionPrice();
        seg.avgDiscountRate = t.getDiscountRate();
        seg.transactionCount = 1;
        seg.representativeName = t.getItemName();
        return seg;
    }

    /**
     * 累加一条成交记录到当前知识片段
     */
    public void accumulate(CompletedTransaction t) {
        int n = this.transactionCount + 1;

        this.avgOriginalPrice = this.avgOriginalPrice
                .multiply(BigDecimal.valueOf(this.transactionCount))
                .add(t.getOriginalPrice())
                .divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

        this.avgTransactionPrice = this.avgTransactionPrice
                .multiply(BigDecimal.valueOf(this.transactionCount))
                .add(t.getTransactionPrice())
                .divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

        this.avgDiscountRate = this.avgDiscountRate
                .multiply(BigDecimal.valueOf(this.transactionCount))
                .add(t.getDiscountRate())
                .divide(BigDecimal.valueOf(n), 4, RoundingMode.HALF_UP);

        if (t.getTransactionPrice().compareTo(this.minTransactionPrice) < 0) {
            this.minTransactionPrice = t.getTransactionPrice();
        }
        if (t.getTransactionPrice().compareTo(this.maxTransactionPrice) > 0) {
            this.maxTransactionPrice = t.getTransactionPrice();
        }

        this.transactionCount = n;
    }
}