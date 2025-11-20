package com.xianzhuan.ai.vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xianzhuan.ai.properties.MilvusProperties;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.common.DataType;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.GetCollectionStatsReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 估价向量库封装（Milvus）
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Component
@Slf4j
public class PricingVectorStore {

    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private MilvusProperties milvusProps;

    private MilvusClientV2 client;

    // Milvus字段名称
    private static final String FIELD_ID = "id";
    private static final String FIELD_VECTOR = "vector";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_CATEGORY_ID = "categoryId";
    private static final String FIELD_BRAND = "brand";
    private static final String FIELD_AVG_PRICE = "avgPrice";
    private static final String FIELD_DISCOUNT_RATE = "discountRate";
    private static final String FIELD_TRANSACTION_COUNT = "transactionCount";
    private static final String FIELD_CONDITION = "condition";

    /**
     * 初始化Milvus连接和Collection
     */
    @PostConstruct
    public void init() {
        try {
            ConnectConfig config = ConnectConfig.builder()
                    .uri("http://" + milvusProps.getHost() + ":" + milvusProps.getPort())
                    .build();
            this.client = new MilvusClientV2(config);
            log.info("Milvus连接初始化完成: {}:{}", milvusProps.getHost(), milvusProps.getPort());

            ensureCollectionExists();
        } catch (Exception e) {
            log.error("Milvus初始化失败，在线估价将降级到规则引擎", e);
        }
    }

    /**
     * 写入向量数据（离线管线调用）
     */
    public void upsert(List<Document> documents) {
        if (documents == null || documents.isEmpty()) return;
        if (client == null) {
            log.warn("Milvus客户端未初始化，跳过向量写入");
            return;
        }

        try {
            List<JsonObject> dataList = new ArrayList<>();

            for (Document doc : documents) {
                float[] embedding = embeddingModel.embed(doc.getText());

                JsonObject data = new JsonObject();
                data.addProperty(FIELD_ID, doc.getId());
                data.add(FIELD_VECTOR, toJsonArray(embedding));
                data.addProperty(FIELD_CONTENT, doc.getText());
                data.addProperty(FIELD_CATEGORY_ID, Long.valueOf(doc.getMetadata().get("categoryId").toString()));
                data.addProperty(FIELD_BRAND, doc.getMetadata().getOrDefault("brand", "").toString());
                data.addProperty(FIELD_AVG_PRICE, Double.valueOf(doc.getMetadata().getOrDefault("avgPrice", "0.0").toString()));
                data.addProperty(FIELD_DISCOUNT_RATE, Double.valueOf(doc.getMetadata().getOrDefault("discountRate", "0.0").toString()));
                data.addProperty(FIELD_TRANSACTION_COUNT, Long.valueOf(doc.getMetadata().getOrDefault("transactionCount", "0").toString()));
                data.addProperty(FIELD_CONDITION, doc.getMetadata().getOrDefault("condition", "").toString());

                dataList.add(data);
            }

            InsertReq insertReq = InsertReq.builder()
                    .collectionName(milvusProps.getCollectionName())
                    .data(dataList)
                    .build();
            client.insert(insertReq);
            log.info("写入 {} 条向量数据到Milvus", documents.size());
        } catch (Exception e) {
            log.error("向量数据写入Milvus失败", e);
        }
    }

    /**
     * 语义相似度检索（在线估价调用）
     *
     * @param queryText  查询文本
     * @param topK       返回最相似的K条结果
     * @param categoryId 品类ID（metadata过滤条件，可选）
     * @return 检索到的Document列表
     */
    public List<Document> search(String queryText, int topK, Long categoryId) {
        if (client == null) {
            log.warn("Milvus客户端未初始化，返回空检索结果");
            return Collections.emptyList();
        }

        try {
            float[] queryEmbedding = embeddingModel.embed(queryText);
            FloatVec queryVector = new FloatVec(queryEmbedding);

            String filterExpr = null;
            if (categoryId != null) {
                filterExpr = String.format("categoryId == %d", categoryId);
            }

            SearchReq.SearchReqBuilder searchReqBuilder = SearchReq.builder()
                    .collectionName(milvusProps.getCollectionName())
                    .data(List.of(queryVector))
                    .topK(topK)
                    .outputFields(List.of(FIELD_CONTENT, FIELD_CATEGORY_ID, FIELD_BRAND,
                            FIELD_AVG_PRICE, FIELD_DISCOUNT_RATE, FIELD_TRANSACTION_COUNT, FIELD_CONDITION));

            if (filterExpr != null) {
                searchReqBuilder.filter(filterExpr);
            }

            SearchResp searchResp = client.search(searchReqBuilder.build());

            List<Document> results = new ArrayList<>();
            List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
            if (searchResults != null && !searchResults.isEmpty()) {
                for (SearchResp.SearchResult hit : searchResults.get(0)) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("categoryId", hit.getEntity().get(FIELD_CATEGORY_ID));
                    metadata.put("brand", hit.getEntity().get(FIELD_BRAND));
                    metadata.put("avgPrice", hit.getEntity().get(FIELD_AVG_PRICE));
                    metadata.put("discountRate", hit.getEntity().get(FIELD_DISCOUNT_RATE));
                    metadata.put("transactionCount", hit.getEntity().get(FIELD_TRANSACTION_COUNT));
                    metadata.put("condition", hit.getEntity().get(FIELD_CONDITION));

                    String content = String.valueOf(hit.getEntity().get(FIELD_CONTENT));
                    String id = String.valueOf(hit.getId());
                    results.add(new Document(id, content, metadata));
                }
            }

            log.info("向量检索: query={}, topK={}, categoryId={}, 命中{}条", queryText, topK, categoryId, results.size());
            return results;

        } catch (Exception e) {
            log.error("向量检索失败, queryText={}", queryText, e);
            return Collections.emptyList();
        }
    }

    /**
     * 确保Milvus Collection存在，不存在则创建
     */
    private void ensureCollectionExists() {
        try {
            GetCollectionStatsReq statsReq = GetCollectionStatsReq.builder()
                    .collectionName(milvusProps.getCollectionName())
                    .build();
            client.getCollectionStats(statsReq);
            log.info("Milvus Collection已存在: {}", milvusProps.getCollectionName());
        } catch (Exception e) {
            log.info("创建Milvus Collection: {}", milvusProps.getCollectionName());

            int dimension = Integer.parseInt(milvusProps.getDimension());

            CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder()
                    .build();
            schema.addField(AddFieldReq.builder()
                    .fieldName(FIELD_ID).dataType(DataType.VarChar).isPrimaryKey(true).autoID(true).maxLength(64).build());
            schema.addField(AddFieldReq.builder()
                    .fieldName(FIELD_VECTOR).dataType(DataType.FloatVector).dimension(dimension).build());
            schema.addField(AddFieldReq.builder()
                    .fieldName(FIELD_CONTENT).dataType(DataType.VarChar).maxLength(1024).build());
            schema.addField(AddFieldReq.builder()
                    .fieldName(FIELD_CATEGORY_ID).dataType(DataType.Int64).build());
            schema.addField(AddFieldReq.builder()
                    .fieldName(FIELD_BRAND).dataType(DataType.VarChar).maxLength(128).build());
            schema.addField(AddFieldReq.builder()
                    .fieldName(FIELD_AVG_PRICE).dataType(DataType.Float).build());
            schema.addField(AddFieldReq.builder()
                    .fieldName(FIELD_DISCOUNT_RATE).dataType(DataType.Float).build());
            schema.addField(AddFieldReq.builder()
                    .fieldName(FIELD_TRANSACTION_COUNT).dataType(DataType.Int64).build());
            schema.addField(AddFieldReq.builder()
                    .fieldName(FIELD_CONDITION).dataType(DataType.VarChar).maxLength(32).build());

            CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                    .collectionName(milvusProps.getCollectionName())
                    .collectionSchema(schema)
                    .build();
            client.createCollection(createCollectionReq);
            log.info("Milvus Collection创建成功: {}", milvusProps.getCollectionName());
        }
    }

    /**
     * 将float[]转为Gson JsonArray
     */
    private JsonArray toJsonArray(float[] arr) {
        JsonArray jsonArray = new JsonArray();
        for (float v : arr) {
            jsonArray.add(v);
        }
        return jsonArray;
    }
}