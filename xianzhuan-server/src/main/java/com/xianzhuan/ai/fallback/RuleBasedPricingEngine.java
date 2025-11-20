package com.xianzhuan.ai.fallback;

import com.xianzhuan.ai.dto.request.PriceEstimationDTO;
import com.xianzhuan.ai.dto.response.PriceEstimationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 基于规则的估价兜底引擎（LLM/向量检索不可用时降级）
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Component
@Slf4j
public class RuleBasedPricingEngine {

    // 成色折损规则得分区间映射
    private static final Map<String, double[]> CONDITION_DISCOUNT_MAP = Map.of(
            "全新", new double[]{0.90, 0.95},
            "九成新", new double[]{0.70, 0.85},
            "八成新", new double[]{0.50, 0.70},
            "七成新", new double[]{0.35, 0.55},
            "六成新", new double[]{0.25, 0.40},
            "五成新以下", new double[]{0.15, 0.30}
    );

    // 默认折损规则得分区间
    private static final double[] DEFAULT_DISCOUNT = new double[]{0.30, 0.50};

    public PriceEstimationVO estimate(PriceEstimationDTO dto) {
        double[] discount = CONDITION_DISCOUNT_MAP.getOrDefault(dto.getCondition(), DEFAULT_DISCOUNT);
        BigDecimal originalPrice = dto.getOriginalPrice();

        BigDecimal minPrice = originalPrice.multiply(BigDecimal.valueOf(discount[0]))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxPrice = originalPrice.multiply(BigDecimal.valueOf(discount[1]))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal recommended = minPrice.add(maxPrice)
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(0.95))
                .setScale(2, RoundingMode.HALF_UP);

        return PriceEstimationVO.builder()
                .suggestedMinPrice(minPrice)
                .suggestedMaxPrice(maxPrice)
                .recommendedPrice(recommended)
                // 基于规则的置信度：LOW低
                .confidence("LOW")
                .explanation(String.format("基于成色折损规则估算：%s建议原价的%.0f%%-%.0f%%。AI估价暂不可用，建议参考同类商品定价。",
                        dto.getCondition(), discount[0] * 100, discount[1] * 100))
                .sampleSize(0)
                .source("RULE")
                .build();
    }
}