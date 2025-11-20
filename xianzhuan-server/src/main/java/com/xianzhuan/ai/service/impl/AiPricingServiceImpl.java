package com.xianzhuan.ai.service.impl;

import com.alibaba.fastjson.JSON;
import com.xianzhuan.ai.dto.request.PriceEstimationDTO;
import com.xianzhuan.ai.dto.response.PriceEstimationVO;
import com.xianzhuan.ai.fallback.RuleBasedPricingEngine;
import com.xianzhuan.ai.prompt.PricingPromptTemplate;
import com.xianzhuan.ai.service.AiPricingService;
import com.xianzhuan.ai.vector.PricingVectorStore;
import com.xianzhuan.context.BaseContext;
import com.xianzhuan.entity.Category;
import com.xianzhuan.mapper.CategoryMapper;
import com.xianzhuan.utils.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * AI智能定价服务实现（RAG架构）
 *
 * 在线估价链路：防重复 → 向量检索 → Prompt组装 → LLM推理
 * 完全不查DB，数据由离线管线预Embedding到向量库
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Service
@Slf4j
public class AiPricingServiceImpl implements AiPricingService {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private PricingPromptTemplate promptTemplate;
    @Autowired
    private RuleBasedPricingEngine ruleEngine;
    @Autowired
    private PricingVectorStore vectorStore;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${xianzhuan.ai.pricing.dedup-ttl-minutes:3}")
    private long dedupTtlMinutes;

    @Value("${xianzhuan.ai.pricing.rag-top-k:5}")
    private int ragTopK;

    @Override
    public PriceEstimationVO estimate(PriceEstimationDTO dto) {
        Long userId = BaseContext.getCurrentId();
        String dedupKey = buildDedupKey(userId, dto);
        try {
            String cached = stringRedisTemplate.opsForValue().get(dedupKey);
            if (cached != null) {
                log.info("智能估价命中防重复缓存, userId={}", userId);
                return JSON.parseObject(cached, PriceEstimationVO.class);
            }
        } catch (Exception e) {
            log.warn("防重复缓存读取失败, key={}", dedupKey, e);
        }

        PriceEstimationVO result = doEstimateWithRag(dto);

        try {
            stringRedisTemplate.opsForValue().set(dedupKey,
                    JSON.toJSONString(result), dedupTtlMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("防重复缓存写入失败, key={}", dedupKey, e);
        }

        return result;
    }

    /**
     * 构建防重复缓存Key
     */
    private String buildDedupKey(Long userId, PriceEstimationDTO dto) {
        int hash = Objects.hash(dto.getCategoryId(), dto.getCondition(),
                dto.getOriginalPrice(), dto.getBrand(), dto.getName());
        return RedisConstant.AI_PRICING_DEDUP_KEY + userId + ":" + hash;
    }

    /**
     * RAG估价核心流程：向量检索 → Prompt组装 → LLM推理
     */
    private PriceEstimationVO doEstimateWithRag(PriceEstimationDTO dto) {
        try {
            Category category = categoryMapper.getById(dto.getCategoryId());
            String categoryName = category != null ? category.getName() : "未知品类";

            String queryText = buildQueryText(dto, categoryName);
            log.info("RAG查询文本: {}", queryText);

            List<Document> ragResults = vectorStore.search(queryText, ragTopK, dto.getCategoryId());
            log.info("RAG检索到 {} 条同类案例", ragResults.size());

            String userPrompt = promptTemplate.buildUserPrompt(dto, categoryName, ragResults);

            String llmResponse = chatClient.prompt()
                    .system(promptTemplate.getSystemPrompt())
                    .user(userPrompt)
                    .call()
                    .content();

            int sampleSize = ragResults.size();
            PriceEstimationVO vo = parseLlmResponse(llmResponse, sampleSize);
            vo.setSource("AI");
            return vo;

        } catch (Exception e) {
            log.error("RAG估价异常, 降级到规则估价, categoryId={}", dto.getCategoryId(), e);
            return ruleEngine.estimate(dto);
        }
    }

    /**
     * 构建RAG查询文本（用于Embedding检索）
     * 将商品名称+品牌+品类+成色组合为自然语言查询
     */
    private String buildQueryText(PriceEstimationDTO dto, String categoryName) {
        StringBuilder sb = new StringBuilder();
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            sb.append(dto.getName()).append(" ");
        }
        if (dto.getBrand() != null && !dto.getBrand().isEmpty()) {
            sb.append(dto.getBrand()).append(" ");
        }
        sb.append(categoryName).append(" ");
        sb.append(dto.getCondition());
        return sb.toString().trim();
    }

    /**
     * 解析LLM响应为PriceEstimationVO
     */
    private PriceEstimationVO parseLlmResponse(String llmResponse, int sampleSize) {
        try {
            String jsonStr = extractJson(llmResponse);

            PriceEstimationVO vo = JSON.parseObject(jsonStr, PriceEstimationVO.class);
            if (vo.getSuggestedMinPrice() == null || vo.getRecommendedPrice() == null) {
                throw new RuntimeException("LLM响应缺少必要字段");
            }
            vo.setSampleSize(sampleSize);
            return vo;
        } catch (Exception e) {
            log.error("LLM响应解析失败, response={}", llmResponse, e);
            throw new RuntimeException("LLM响应解析失败", e);
        }
    }

    /**
     * 从LLM响应文本中提取JSON字符串
     */
    private String extractJson(String response) {
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        throw new RuntimeException("LLM响应中未找到JSON");
    }
}