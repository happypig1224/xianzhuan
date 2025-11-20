package com.xianzhuan.ai.pipeline;

import com.alibaba.fastjson.JSON;
import com.xianzhuan.ai.vector.PricingVectorStore;
import com.xianzhuan.mapper.PricingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 估价数据离线管线：导出 → 清洗聚合 → Embedding → 入向量库
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Component
@Slf4j
public class PricingDataPipeline {

    @Autowired
    private PricingDataMapper pricingDataMapper;
    @Autowired
    private PricingVectorStore vectorStore;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 每小时执行一次数据管线
     * cron表达式由配置控制，默认整点执行
     */
    @Scheduled(cron = "${xianzhuan.ai.pricing.pipeline-cron:0 0 * * * ?}")
    public void execute() {
        long startTime = System.currentTimeMillis();
        log.info("==== 估价数据管线开始执行 ====");

        try {
            List<CompletedTransaction> transactions = pricingDataMapper.queryAllCompletedTransactions();
            log.info("导出成交记录 {} 条", transactions.size());

            if (transactions.isEmpty()) {
                log.info("无成交数据，跳过管线执行");
                return;
            }

            List<PricingKnowledgeSegment> segments = aggregateAndClean(transactions);
            log.info("清洗聚合为 {} 个知识片段", segments.size());

            List<Document> documents = buildDocuments(segments);
            vectorStore.upsert(documents);
            log.info("Embedding并写入向量库 {} 条", documents.size());

            long elapsed = System.currentTimeMillis() - startTime;
            recordPipelineStatus(transactions.size(), segments.size(), elapsed, "SUCCESS");
            log.info("==== 估价数据管线执行完成, 耗时{}ms ====", elapsed);

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            recordPipelineStatus(0, 0, elapsed, "FAILED:" + e.getMessage());
            log.error("估价数据管线执行失败", e);
        }
    }

    /**
     * 按品牌+品类+成色分组聚合
     */
    private List<PricingKnowledgeSegment> aggregateAndClean(List<CompletedTransaction> transactions) {
        Map<String, PricingKnowledgeSegment> segmentMap = new HashMap<>();

        for (CompletedTransaction t : transactions) {
            // 提取品牌（从商品名称中提取，或使用已知品牌匹配）
            String brand = extractBrand(t.getItemName());

            // 构建分组键
            String groupKey = brand + "|" + t.getCategoryId() + "|" + t.getCondition();

            // 累加聚合
            segmentMap.compute(groupKey, (k, existing) -> {
                if (existing == null) {
                    return PricingKnowledgeSegment.fromFirst(t, brand);
                } else {
                    existing.accumulate(t);
                    return existing;
                }
            });
        }

        return new ArrayList<>(segmentMap.values());
    }

    /**
     * 将知识片段转化为向量库Document
     */
    private List<Document> buildDocuments(List<PricingKnowledgeSegment> segments) {
        List<Document> documents = new ArrayList<>();

        for (PricingKnowledgeSegment seg : segments) {
            // 构建查询用文本（这是Embedding的输入，也是检索匹配的目标）
            String content = String.format(
                    "%s %s %s 原价%.0f 成交均价%.0f 成交%d笔 折价率%.0f%%",
                    seg.getRepresentativeName(),
                    seg.getCategoryName(),
                    seg.getCondition(),
                    seg.getAvgOriginalPrice().doubleValue(),
                    seg.getAvgTransactionPrice().doubleValue(),
                    seg.getTransactionCount(),
                    seg.getAvgDiscountRate().doubleValue() * 100
            );

            // 构建metadata（用于向量检索时的过滤条件）
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("categoryId", seg.getCategoryId());
            metadata.put("brand", seg.getBrand());
            metadata.put("avgPrice", seg.getAvgTransactionPrice().doubleValue());
            metadata.put("discountRate", seg.getAvgDiscountRate().doubleValue());
            metadata.put("transactionCount", seg.getTransactionCount());
            metadata.put("condition", seg.getCondition());

            documents.add(new Document(content, metadata));
        }

        return documents;
    }

    /**
     * 从商品名称中提取品牌（简易规则，后续可优化为LLM提取）
     */
    private String extractBrand(String itemName) {
        if (itemName == null) return "未知";
        String upper = itemName.toUpperCase();
        String[] knownBrands = {"NIKE", "ADIDAS", "IPHONE", "IPAD", "MACBOOK", "HUAWEI",
                "XIAOMI", "SAMSUNG", "SONY", "APPLE", "PUMA", "NEW BALANCE"};
        for (String brand : knownBrands) {
            if (upper.contains(brand)) return brand;
        }
        return "其他";
    }

    /**
     * 记录管线执行状态到Redis
     */
    private void recordPipelineStatus(int totalRecords, int segments, long elapsed, String status) {
        try {
            String statusJson = JSON.toJSONString(Map.of(
                    "lastExecuteTime", System.currentTimeMillis(),
                    "totalRecords", totalRecords,
                    "segments", segments,
                    "elapsedMs", elapsed,
                    "status", status
            ));
            stringRedisTemplate.opsForValue().set("ai:pipeline:status", statusJson, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("管线状态记录失败", e);
        }
    }
}