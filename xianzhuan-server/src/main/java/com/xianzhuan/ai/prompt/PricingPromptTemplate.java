package com.xianzhuan.ai.prompt;

import com.xianzhuan.ai.dto.request.PriceEstimationDTO;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能定价Prompt模板
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Component
public class PricingPromptTemplate {

    private static final String SYSTEM_PROMPT = """
            你是一个专业的二手商品估价师。你需要根据商品信息和检索到的同类商品成交案例，给出合理的二手定价建议。

            估价原则：
            1. 以检索到的同类成交案例为首要参考依据，案例越相似权重越高
            2. 成色是影响折价率的核心因素：全新90%-95%、九成新70%-85%、八成新50%-70%、七成新35%-55%、六成新以下20%-40%
            3. 品牌溢价：知名品牌折价率上浮5%-10%，杂牌下浮5%-10%
            4. 供需关系：近期成交活跃的品类可适当上浮
            5. 必须给出价格区间和推荐价，推荐价应在区间内偏向成交概率最高的价位

            输出要求：
            - 严格按照JSON格式输出，不要输出任何其他内容
            - JSON字段：suggestedMinPrice(最低价), suggestedMaxPrice(最高价), recommendedPrice(推荐价), confidence(HIGH/MEDIUM/LOW), explanation(估价依据，50字以内)
            - 价格单位为元，保留两位小数
            - confidence: 参考案例≥20条为HIGH，≥5条为MEDIUM，<5条为LOW
            """;

    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    /**
     * 构建用户提示词
     */
    public String buildUserPrompt(PriceEstimationDTO dto,
                                  String categoryName,
                                  List<Document> ragResults) {
        String ragFormatted = "（无同类成交案例）";
        if (ragResults != null && !ragResults.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ragResults.size(); i++) {
                Document doc = ragResults.get(i);
                sb.append(String.format("  %d. %s\n", i + 1, doc.getText()));
            }
            ragFormatted = sb.toString();
        }

        return String.format("""
                请为以下二手商品估价：

                【商品信息】
                - 名称：%s
                - 品类：%s
                - 成色：%s
                - 原价：%s元
                - 品牌：%s

                【同类商品成交案例】（按相似度排序）
                %s

                请基于以上成交案例给出定价建议，严格按JSON格式输出。
                """,
                dto.getName() != null ? dto.getName() : "未提供",
                categoryName,
                dto.getCondition(),
                dto.getOriginalPrice(),
                dto.getBrand() != null ? dto.getBrand() : "未提供",
                ragFormatted);
    }
}