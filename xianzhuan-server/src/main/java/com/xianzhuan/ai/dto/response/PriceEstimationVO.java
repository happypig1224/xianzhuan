package com.xianzhuan.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceEstimationVO {
    private BigDecimal suggestedMinPrice; // 建议最低价，单位：元
    private BigDecimal suggestedMaxPrice; // 建议最高价，单位：元
    private BigDecimal recommendedPrice; // 推荐价，单位：元
    private String confidence; // 置信度，HIGH/MEDIUM/LOW
    private String explanation; // 估价依据，自然语言描述
    private Integer sampleSize; // 参考的同类成交记录数
    private String source; //估价来源 区分大模型和规则匹配
}
