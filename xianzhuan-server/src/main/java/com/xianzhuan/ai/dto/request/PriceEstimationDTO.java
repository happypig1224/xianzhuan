package com.xianzhuan.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 价格估价请求DTO
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceEstimationDTO {
    private Long categoryId; // 商品分类ID
    private String condition; // 商品成色描述，示例："全新"、"九成新"、"八成新"、"七成新"、"六成新"、"五成新"、"四成新"、"三成新"、"二成
    private BigDecimal originalPrice; // 商品原始价格，单位：元
    private String brand; // 商品品牌，示例："Apple"、"Samsung"、"Google"等
    private String name; // 商品名称，用于语义匹配同类商品
}
