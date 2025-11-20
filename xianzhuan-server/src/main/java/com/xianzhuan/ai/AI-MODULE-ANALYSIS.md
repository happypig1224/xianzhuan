# AI智能估价模块 -- 代码实现细节与踩坑经验

> @author Wu, Hui Ming  
> @version 1.0  
> @School Suihua University

---

## 一、模块总览

本模块实现了**基于RAG（Retrieval-Augmented Generation）架构的二手商品智能估价系统**，核心技术栈：

| 技术组件 | 选型 |
|---------|------|
| LLM大模型 | 阿里通义千问 `qwen-plus`（通过Spring AI DashScope接入） |
| Embedding模型 | 阿里 `text-embedding-v3` |
| 向量数据库 | Milvus 2.x（MilvusClientV2） |
| 缓存/去重 | Redis（StringRedisTemplate） |
| 离线数据源 | MySQL（MyBatis） |
| 框架 | Spring AI + Spring Boot |

**核心架构决策**：在线估价链路完全不查DB，数据由离线管线预Embedding到向量库，实现了**在线/离线解耦**的架构。

---

## 二、逐包逐类实现细节

### 2.1 config 包 — 配置层

#### `ChatClientConfig.java`

**职责**：将Spring AI自动装配的`ChatModel`（底层是DashScope的qwen-plus）包装为`ChatClient` Bean，供Service层注入使用。

**核心代码**：

```java
@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
```

**设计模式**：工厂方法模式 — 通过`@Bean`工厂方法创建ChatClient实例，将ChatModel的创建细节封装。

**实现细节**：
- Spring AI的`ChatClient`是Fluent API风格的门面类，相比直接使用`ChatModel`，提供了`.prompt().system().user().call()`链式调用，代码更简洁
- `ChatClient.builder(chatModel).build()`是最简配置，未设置`defaultAdvisor`、`defaultSystem`等默认行为，意味着每次调用都需要显式设置system prompt

**⚠️ 踩坑经验**：
- 如果未来需要全局默认System Prompt或对话记忆（ChatMemory），应在此处配置`defaultAdvisors`，否则每个调用点都要重复设置
- 当前每次调用都显式传入system prompt，虽然灵活但存在冗余，如果System Prompt需要全局修改，需要改所有调用点

---

### 2.2 properties 包 — 配置属性层

#### `MilvusProperties.java`

**职责**：将`xianzhuan.ai.milvus.*`前缀的YAML配置映射为类型安全的Java Bean。

**核心代码**：

```java
@Component
@Data
@ConfigurationProperties(prefix = "xianzhuan.ai.milvus")
public class MilvusProperties {
    private String host;
    private int port;
    private String collectionName;
    private String dimension;
}
```

**设计模式**：类型安全配置模式（Type-Safe Configuration Pattern），Spring Boot标准的`@ConfigurationProperties`用法。

**⚠️ 踩坑经验**：
- `dimension`字段声明为`String`而非`int`，在`PricingVectorStore.ensureCollectionExists()`中通过`Integer.parseInt()`转换。这是一个**设计隐患**：如果配置值非数字，运行时才抛异常。建议改为`int`类型，利用Spring Boot的配置绑定自动校验
- 缺少`@Validated`注解，无法在启动时校验host/port/collectionName是否为空
- Milvus连接没有配置`username`/`password`/`token`字段，生产环境Milvus通常需要认证，当前只支持无认证模式

---

### 2.3 dto 包 — 数据传输对象层

#### `PriceEstimationDTO.java`（请求DTO）

**职责**：封装C端用户提交的估价请求参数。

**核心字段**：

```java
@Builder @Data @AllArgsConstructor @NoArgsConstructor
public class PriceEstimationDTO {
    private Long categoryId;        // 商品分类ID
    private String condition;       // 商品成色描述
    private BigDecimal originalPrice; // 商品原始价格
    private String brand;           // 商品品牌
    private String name;            // 商品名称
}
```

**实现细节**：
- 使用`BigDecimal`表示价格，避免了浮点精度问题，这是金融/价格场景的**最佳实践**
- `condition`字段是自由文本，没有枚举约束。好处是灵活，坏处是用户可能输入"还行"、"一般"等非标准成色描述，导致规则引擎匹配不到，向量检索语义也可能偏差

**⚠️ 踩坑经验**：
- 缺少`@NotNull`/`@NotBlank`等JSR303校验注解，Controller层也没有`@Valid`，意味着空请求体也能到达Service层，需要Service层自行判空
- `brand`字段是可选的，当用户不填时，RAG查询文本构建会跳过品牌，这会导致检索结果可能不够精准

#### `PriceEstimationVO.java`（响应VO）

**职责**：封装估价结果返回给前端。

**核心字段**：

```java
@Builder @Data @AllArgsConstructor @NoArgsConstructor
public class PriceEstimationVO {
    private BigDecimal suggestedMinPrice;  // 建议最低价
    private BigDecimal suggestedMaxPrice;  // 建议最高价
    private BigDecimal recommendedPrice;   // 推荐价
    private String confidence;             // 置信度 HIGH/MEDIUM/LOW
    private String explanation;            // 估价依据
    private Integer sampleSize;            // 参考成交记录数
    private String source;                 // 估价来源 AI/RULE
}
```

**设计亮点**：
- `source`字段区分了"AI"和"RULE"两种估价来源，前端可以据此展示不同UI（如AI估价显示"AI智能估价"标签，规则估价显示"参考估价"标签）
- `confidence`字段让前端可以展示置信度提示，增强用户信任感
- `sampleSize`让用户知道参考了多少条数据，透明度高

**⚠️ 踩坑经验**：
- `confidence`是String类型而非枚举，LLM可能输出"high"（小写）或"High"（首字母大写），与前端约定不一致。建议在`parseLlmResponse`中做标准化处理
- 缺少`estimatedAt`时间戳字段，无法追踪估价时间

---

### 2.4 controller 包 — 控制器层

#### `PriceEstimationController.java`

**职责**：接收C端HTTP请求，委派给Service层处理，返回统一响应格式。

**核心代码**：

```java
@RestController
@RequestMapping("/user/priceEstimation")
@Tag(name = "C端-AI智能估价接口")
public class PriceEstimationController {
    @Autowired
    private AiPricingService aiPricingService;

    @PostMapping("/estimate-price")
    @Operation(summary = "AI智能估价")
    public Result<PriceEstimationVO> estimatePrice(@RequestBody PriceEstimationDTO dto) {
        log.info("智能估价请求: categoryId={}, condition={}, originalPrice={}, name={}",
                dto.getCategoryId(), dto.getCondition(), dto.getOriginalPrice(), dto.getName());
        PriceEstimationVO vo = aiPricingService.estimate(dto);
        return Result.success(vo);
    }
}
```

**设计模式**：委派模式 — Controller只做参数接收和结果包装，不包含业务逻辑。

**⚠️ 踩坑经验**：
- 使用`@Autowired`字段注入而非构造器注入。Spring官方推荐构造器注入，因为：1) 不可变性好 2) 依赖关系显式化 3) 便于单元测试
- 日志中打印了`originalPrice`和`name`，但**没有脱敏处理**。虽然价格不是敏感信息，但商品名称可能包含个人隐私
- 缺少参数校验（`@Valid` + DTO上的`@NotNull`等注解），如果`categoryId`或`originalPrice`为null，会在Service层才暴露问题
- 接口路径`/user/priceEstimation`暗示需要用户登录（有userId），但Controller层没有看到权限校验注解，可能依赖全局拦截器

---

### 2.5 fallback 包 — 降级兜底层

#### `RuleBasedPricingEngine.java`

**职责**：当LLM/向量检索不可用时，提供基于规则的纯本地估价兜底方案。

**核心逻辑**：

```java
@Component
public class RuleBasedPricingEngine {
    private static final Map<String, double[]> CONDITION_DISCOUNT_MAP = Map.of(
        "全新", new double[]{0.90, 0.95},
        "九成新", new double[]{0.70, 0.85},
        "八成新", new double[]{0.50, 0.70},
        "七成新", new double[]{0.35, 0.55},
        "六成新", new double[]{0.25, 0.40},
        "五成新以下", new double[]{0.15, 0.30}
    );

    public PriceEstimationVO estimate(PriceEstimationDTO dto) {
        double[] discount = CONDITION_DISCOUNT_MAP.getOrDefault(dto.getCondition(), DEFAULT_DISCOUNT);
        // 最低价 = 原价 × 折损率下限
        // 最高价 = 原价 × 折损率上限
        // 推荐价 = (最低价 + 最高价) / 2 × 0.95（偏向低价促成交）
        // 置信度固定为LOW，来源标记为RULE
    }
}
```

**设计模式**：策略模式的简化实现 — 与AI估价策略形成互补，在`AiPricingServiceImpl`中通过try-catch实现策略切换。

**实现细节**：
1. 根据成色描述查找折损率区间（如"九成新"对应原价的70%-85%）
2. 最低价 = 原价 × 折损率下限，最高价 = 原价 × 折损率上限
3. 推荐价 = (最低价 + 最高价) / 2 × 0.95，即中位数再打95折，偏向低价促成交
4. 置信度固定为"LOW"，来源标记为"RULE"

**⚠️ 踩坑经验**：
- `Map.of()`创建的是不可变Map，线程安全，但**最多只能放10个键值对**。当前6个刚好在限制内，但如果增加成色等级需要注意此限制
- 折损率区间与System Prompt中的估价原则完全一致（全新90%-95%、九成新70%-85%等），这保证了**AI估价和规则估价的基准一致性**
- `DEFAULT_DISCOUNT = {0.30, 0.50}`处理了未知成色的情况，但用户输入"九成旧"、"8成新"等变体时无法匹配，会走默认值
- `dto.getCondition()`可能为null，`Map.of()`不允许null key，会返回DEFAULT_DISCOUNT。虽然结果正确，但语义上应该先判空
- 推荐价的0.95系数是硬编码的"促成交偏向"，这个业务规则没有暴露为配置项

---

### 2.6 pipeline 包 — 离线数据管线层 ⭐⭐

这是本模块最核心的包之一，实现了ETL（Extract-Transform-Load）管线。

#### `CompletedTransaction.java`

**职责**：成交记录的领域模型，对应SQL查询结果的一行。

**核心字段**：

```java
@Data @AllArgsConstructor @NoArgsConstructor
public class CompletedTransaction {
    private Long itemId;
    private String itemName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal originalPrice;
    private BigDecimal transactionPrice;
    private String condition;
    private BigDecimal discountRate;      // SQL计算: ROUND(od.amount / ui.original_price, 4)
    private LocalDateTime checkoutTime;
}
```

**实现细节**：
- `discountRate`由SQL计算：`ROUND(od.amount / ui.original_price, 4)`，即成交价/原价，保留4位小数
- `checkoutTime`用于排序（SQL中`ORDER BY checkout_time DESC`），但当前管线未按时间窗口过滤，每次全量导出

#### `PricingKnowledgeSegment.java`

**职责**：按"品牌+品类+成色"分组聚合后的知识片段，是向量库中一条Document的数据来源。

**核心字段**：

```java
@Data
public class PricingKnowledgeSegment {
    private String groupKey;              // 组合键: brand|categoryId|condition
    private String brand;
    private Long categoryId;
    private String categoryName;
    private String condition;
    private BigDecimal avgOriginalPrice;   // 平均原价
    private BigDecimal avgTransactionPrice; // 平均成交价
    private BigDecimal minTransactionPrice; // 最低成交价
    private BigDecimal maxTransactionPrice; // 最高成交价
    private BigDecimal avgDiscountRate;     // 平均折价率
    private int transactionCount;          // 成交笔数
    private String representativeName;     // 代表商品名称
}
```

**设计模式**：Builder模式的变体 — `fromFirst()`静态工厂方法 + `accumulate()`累加器方法，实现了流式聚合。

**核心逻辑 — `accumulate()`方法的增量均值计算**：

```java
// 增量均值公式: avg_new = (avg_old × n_old + value_new) / n_new
this.avgOriginalPrice = this.avgOriginalPrice
    .multiply(BigDecimal.valueOf(this.transactionCount))
    .add(t.getOriginalPrice())
    .divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
```

这是经典的**在线算法（Welford's method的变体）**，避免了需要保存所有原始数据再求均值的问题，内存效率高。

**⚠️ 踩坑经验**：
- `fromFirst()`中`groupKey`的构建逻辑：`brand + "|" + t.getCategoryId() + "|" + t.getCondition()`，与`PricingDataPipeline.aggregateAndClean()`中的`groupKey`构建逻辑完全一致。但两处代码是**复制粘贴**的，如果修改一处忘记修改另一处会导致分组不一致。建议将groupKey构建逻辑提取为`CompletedTransaction`或`PricingKnowledgeSegment`的静态方法
- `representativeName`只取第一条记录的商品名称，如果第一条记录名称不规范（如"iPhone 14 Pro Max 256G 暗紫色 全网通"），可能影响向量检索的语义质量
- `minTransactionPrice`/`maxTransactionPrice`的更新使用了`compareTo`，BigDecimal比较的正确做法，避免了`equals`的精度陷阱

#### `PricingDataPipeline.java`

**职责**：离线数据管线的调度入口，执行"导出 → 清洗聚合 → Embedding → 入向量库"全流程。

**设计模式**：模板方法模式的简化 — `execute()`方法定义了管线的执行步骤顺序，各步骤通过私有方法实现。

**核心流程**：

```
execute()
  ├── 1. pricingDataMapper.queryAllCompletedTransactions()  // 从MySQL全量导出
  ├── 2. aggregateAndClean(transactions)                     // 按品牌+品类+成色聚合
  │     └── extractBrand(itemName)                           // 从商品名提取品牌
  ├── 3. buildDocuments(segments)                            // 转为Spring AI Document
  │     ├── content = "代表商品名 品类 成色 原价X 成交均价Y 成交Z笔 折价率W%"
  │     └── metadata = {categoryId, brand, avgPrice, discountRate, transactionCount, condition}
  ├── 4. vectorStore.upsert(documents)                       // Embedding + 写入Milvus
  └── 5. recordPipelineStatus(...)                           // 记录执行状态到Redis
```

**实现细节**：

1. **定时调度**：`@Scheduled(cron = "${xianzhuan.ai.pricing.pipeline-cron:0 0 * * * ?}")`，默认每小时整点执行，cron表达式可通过配置覆盖。需要在启动类上有`@EnableScheduling`才能生效

2. **Document内容构建**：content文本的格式`"iPhone 14 Pro Max 手机 九成新 原价7999 成交均价5999 成交15笔 折价率75%"`是精心设计的，它同时服务于：
   - **Embedding输入**：语义信息越丰富，向量表示越准确
   - **RAG检索结果展示**：直接作为LLM的参考案例文本
   - 这是一个**值得学习的设计**：Document的content字段既是索引对象也是检索结果

3. **管线状态记录**：使用Redis存储最近一次执行状态，TTL为1小时。这是一个轻量级的监控方案，但缺少历史执行记录，无法做趋势分析

**⚠️ 踩坑经验**：

1. **全量导出问题**：`queryAllCompletedTransactions()`每次全量查询所有成交记录。当数据量增长到百万级时：
   - SQL查询可能超时
   - 内存可能溢出（所有记录加载到JVM堆）
   - **建议**：改为增量导出，基于上次管线执行时间`WHERE checkout_time > #{lastExecuteTime}`

2. **upsert语义缺失**：`vectorStore.upsert()`方法名暗示"更新或插入"，但实际调用的是`client.insert(insertReq)`，Milvus的insert是追加写入，不是真正的upsert。如果同一组知识片段在多次管线执行中重复写入，会导致**数据膨胀**。需要在写入前先删除旧数据或使用Milvus的upsert API

3. **品牌提取**：`extractBrand()`使用硬编码的已知品牌列表做字符串匹配，这是最简单的NER（命名实体识别）实现。注释中提到"后续可优化为LLM提取"，这是一个合理的演进路径

---

### 2.7 prompt 包 — Prompt工程层 ⭐

#### `PricingPromptTemplate.java`

**职责**：封装System Prompt和User Prompt的模板构建逻辑。

**设计模式**：模板方法模式 — `getSystemPrompt()`提供固定模板，`buildUserPrompt()`根据动态参数填充模板。

**System Prompt深度解析**：

```
你是一个专业的二手商品估价师。你需要根据商品信息和检索到的同类商品成交案例，给出合理的二手定价建议。

估价原则：
1. 以检索到的同类成交案例为首要参考依据，案例越相似权重越高
2. 成色是影响折价率的核心因素：全新90%-95%、九成新70%-85%、八成新50%-70%、七成新35%-55%、六成新以下20%-40%
3. 品牌溢价：知名品牌折价率上浮5%-10%，杂牌下浮5%-10%
4. 供需关系：近期成交活跃的品类可适当上浮
5. 必须给出价格区间和推荐价，推荐价应在区间内偏向成交概率最高的价位

输出要求：
- 严格按照JSON格式输出，不要输出任何其他内容
- JSON字段：suggestedMinPrice, suggestedMaxPrice, recommendedPrice, confidence(HIGH/MEDIUM/LOW), explanation(50字以内)
- 价格单位为元，保留两位小数
- confidence: 参考案例>=20条为HIGH，>=5条为MEDIUM，<5条为LOW
```

**Prompt工程亮点**：
1. **角色设定**："你是一个专业的二手商品估价师" — 明确了LLM的角色定位
2. **估价原则约束**：5条原则给出了清晰的决策框架，特别是第2条成色折损率与规则引擎完全一致，保证了AI估价不会偏离业务常识太远
3. **结构化输出**：强制JSON格式输出，便于程序解析。`extractJson()`方法通过`indexOf("{")`/`lastIndexOf("}")`提取JSON，处理了LLM可能输出"```json...```"的情况
4. **置信度量化标准**：">=20条HIGH, >=5条MEDIUM, <5条LOW"给出了可计算的置信度规则

**⚠️ 踩坑经验**：
- LLM输出JSON的可靠性问题：即使Prompt要求"严格按照JSON格式输出"，LLM仍可能输出带Markdown代码块包裹的JSON、带注释的JSON、或格式错误的JSON。`extractJson()`方法只能处理最简单的情况（前后有多余文本），无法处理JSON内部格式错误
- **建议**：使用Spring AI的`BeanOutputConverter`或Structured Output功能，让框架处理JSON解析和校验，比自己写`extractJson()`更健壮
- System Prompt中"不要输出任何其他内容"的约束，对qwen-plus来说大部分场景有效，但不能100%保证。生产环境需要更健壮的解析逻辑
- `explanation`限制50字以内是好的设计，控制了LLM输出的token消耗

---

### 2.8 vector 包 — 向量存储层 ⭐⭐

#### `PricingVectorStore.java`

**职责**：封装Milvus向量数据库的连接初始化、Collection管理、向量写入和语义检索。

**设计模式**：门面模式 — 对MilvusClientV2的复杂API进行了简化封装，只暴露`upsert()`和`search()`两个高层接口。

**核心方法深度解析**：

**1. `init()` — 初始化连接和Collection**

```java
@PostConstruct
public void init() {
    ConnectConfig config = ConnectConfig.builder()
        .uri("http://" + milvusProps.getHost() + ":" + milvusProps.getPort())
        .build();
    this.client = new MilvusClientV2(config);
    ensureCollectionExists();
}
```

- 使用`@PostConstruct`在Bean初始化后自动执行，保证了Service层注入时Milvus已就绪
- `ensureCollectionExists()`通过"先查询，失败则创建"的方式确保Collection存在，这是一种**乐观初始化**策略

**2. `upsert()` — 向量写入**

```java
public void upsert(List<Document> documents) {
    for (Document doc : documents) {
        float[] embedding = embeddingModel.embed(doc.getText());  // 调用Embedding API
        JsonObject data = new JsonObject();
        data.addProperty(FIELD_ID, doc.getId());
        data.add(FIELD_VECTOR, toJsonArray(embedding));
        data.addProperty(FIELD_CONTENT, doc.getText());
        // ... metadata字段
        dataList.add(data);
    }
    client.insert(insertReq);
}
```

**3. `search()` — 语义检索**

```java
public List<Document> search(String queryText, int topK, Long categoryId) {
    float[] queryEmbedding = embeddingModel.embed(queryText);
    FloatVec queryVector = new FloatVec(queryEmbedding);

    String filterExpr = null;
    if (categoryId != null) {
        filterExpr = String.format("categoryId == %d", categoryId);
    }

    SearchReq searchReq = SearchReq.builder()
        .collectionName(milvusProps.getCollectionName())
        .data(List.of(queryVector))
        .topK(topK)
        .outputFields(List.of(FIELD_CONTENT, FIELD_CATEGORY_ID, ...))
        .filter(filterExpr)  // metadata过滤
        .build();
}
```

- **混合检索策略**：语义相似度（向量距离）+ metadata过滤（categoryId精确匹配），这是RAG中的经典做法
- `filterExpr = "categoryId == %d"`是Milvus的标量过滤表达式语法，在向量检索之前先缩小候选集，提升检索精度

**4. `ensureCollectionExists()` — Collection自动创建**

```java
private void ensureCollectionExists() {
    try {
        client.getCollectionStats(statsReq);  // 尝试获取统计信息
    } catch (Exception e) {
        // Collection不存在，创建
        CreateCollectionReq.CollectionSchema schema = ...;
        schema.addField(AddFieldReq.builder().fieldName(FIELD_ID)
            .dataType(DataType.VarChar).isPrimaryKey(true).autoID(true).maxLength(64).build());
        schema.addField(AddFieldReq.builder().fieldName(FIELD_VECTOR)
            .dataType(DataType.FloatVector).dimension(dimension).build());
        // ... 其他字段
        client.createCollection(createCollectionReq);
    }
}
```

- Schema设计合理：ID为主键（VarChar, autoID）、vector为FloatVector、其他为标量字段用于过滤

**⚠️ 踩坑经验**：

1. **优雅降级设计值得学习**：如果Milvus服务不可用，`init()`会catch异常并只打印日志，`client`字段可能为null。后续`upsert()`和`search()`都做了`if (client == null)`的防御性检查，返回空结果，进而触发规则引擎降级

2. **每条Document单独调用`embeddingModel.embed()`**，没有批量Embedding。当知识片段数量较多时（如数千条），会频繁调用DashScope API，存在**限流风险**和**性能瓶颈**

3. **方法名叫`upsert`但实际调用的是`insert`**，语义不一致。Milvus 2.x支持`upsert()`方法，应该使用它来避免重复数据

4. **只按categoryId过滤**，没有按condition（成色）过滤。如果用户查"九成新iPhone"，可能检索到"全新iPhone"的案例，虽然语义相似但折价率差异大。建议增加condition过滤或加权

5. **没有配置相似度阈值**（score threshold），可能返回不相关的结果。Milvus支持设置`searchParams`中的`metric_type`和距离阈值

6. **通过try-catch来判断Collection是否存在**不是最佳实践。Milvus SDK提供了`hasCollection()`方法，应该使用它。try-catch方式的问题是：如果异常是因为网络问题而非Collection不存在，会错误地尝试创建

7. **创建Collection后没有创建索引**。Milvus默认会创建一个FLAT索引，对于小数据量可以工作，但生产环境必须创建IVF_FLAT或HNSW索引才能保证检索性能

8. **Milvus的`autoID(true)`**意味着不需要手动指定ID，但代码中`data.addProperty(FIELD_ID, doc.getId())`仍然写入了ID字段，可能导致冲突或被忽略

---

### 2.9 service 包 — 服务层 ⭐⭐⭐

#### `AiPricingService.java`（接口）

**职责**：定义AI智能定价服务的唯一接口。

**设计模式**：接口隔离原则（ISP）— 只暴露一个方法，接口极度精简。

#### `AiPricingServiceImpl.java`（核心实现）

**职责**：RAG估价的核心业务逻辑实现，包含防重复、向量检索、Prompt组装、LLM推理、响应解析、降级兜底。

**核心流程**：

```
estimate(dto)
  ├── 1. 防重复检查（Redis缓存，TTL=3分钟）
  │     ├── 命中缓存 → 直接返回
  │     └── 未命中 ↓
  ├── 2. doEstimateWithRag(dto)
  │     ├── 2.1 查询品类名称（categoryMapper.getById）
  │     ├── 2.2 构建RAG查询文本（name + brand + categoryName + condition）
  │     ├── 2.3 向量检索（vectorStore.search, topK=5）
  │     ├── 2.4 组装User Prompt（promptTemplate.buildUserPrompt）
  │     ├── 2.5 LLM推理（chatClient.prompt().system().user().call()）
  │     ├── 2.6 解析LLM响应（extractJson + JSON.parseObject）
  │     └── 2.7 异常 → 降级到ruleEngine.estimate()
  ├── 3. 写入防重复缓存（Redis, TTL=3分钟）
  └── 4. 返回结果
```

**设计模式**：
- **责任链模式的简化**：防重复 → RAG估价 → 规则兜底，形成三级处理链
- **降级模式（Fallback Pattern）**：RAG异常时自动降级到规则引擎，保证服务可用性

**实现细节与踩坑经验**：

**1. 防重复缓存**：

```java
String dedupKey = RedisConstant.AI_PRICING_DEDUP_KEY + userId + ":" + hash;
```

- Key由`userId + Objects.hash(categoryId, condition, originalPrice, brand, name)`组成
- TTL默认3分钟，通过`xianzhuan.ai.pricing.dedup-ttl-minutes`配置
- **⚠️ 踩坑**：`Objects.hash()`可能产生哈希碰撞，不同参数组合可能得到相同hash值。虽然概率极低，但在高并发场景下可能导致用户看到别人的估价结果。建议改用参数拼接作为Key的一部分
- **⚠️ 踩坑**：防重复缓存的读写都用了try-catch包裹，Redis异常不影响主流程。这是**防御性编程**的好实践

**2. RAG查询文本构建**：

```java
private String buildQueryText(PriceEstimationDTO dto, String categoryName) {
    StringBuilder sb = new StringBuilder();
    if (dto.getName() != null) sb.append(dto.getName()).append(" ");
    if (dto.getBrand() != null) sb.append(dto.getBrand()).append(" ");
    sb.append(categoryName).append(" ").append(dto.getCondition());
    return sb.toString().trim();
}
```

- 查询文本 = 商品名 + 品牌 + 品类 + 成色，这是**多维度语义查询**的设计
- **⚠️ 踩坑**：查询文本没有包含originalPrice信息。原价是影响估价的重要因素，但向量检索是基于语义相似度的，数值信息在Embedding中表达较弱。建议在metadata过滤中增加价格区间过滤

**3. LLM调用**：

```java
String llmResponse = chatClient.prompt()
    .system(promptTemplate.getSystemPrompt())
    .user(userPrompt)
    .call()
    .content();
```

- 使用Spring AI的Fluent API，简洁清晰
- `.call()`是同步阻塞调用，会等待LLM完整响应。qwen-plus的响应时间通常在1-3秒，如果并发量大，会占用大量线程
- **建议**：考虑使用`.stream()`流式响应，或者使用异步调用+CompletableFuture，提升吞吐量

**4. LLM响应解析**：

```java
private String extractJson(String response) {
    int start = response.indexOf("{");
    int end = response.lastIndexOf("}");
    if (start >= 0 && end > start) {
        return response.substring(start, end + 1);
    }
    throw new RuntimeException("LLM响应中未找到JSON");
}
```

- 这是最简的JSON提取逻辑，能处理LLM在JSON前后添加文字的情况
- **⚠️ 踩坑**：如果LLM输出了嵌套JSON（如explanation字段中包含`{}`），`lastIndexOf("}")`可能截取到内层的`}`，导致JSON不完整。更健壮的做法是使用正则或JSON解析器逐字符匹配
- **⚠️ 踩坑**：`parseLlmResponse()`中`JSON.parseObject(jsonStr, PriceEstimationVO.class)`依赖Fastjson将JSON字段名映射到Java字段名（驼峰），但LLM输出的JSON字段名是`suggestedMinPrice`，与VO字段名一致，所以能正确映射。但如果LLM输出`min_price`等蛇形命名就会映射失败

**5. 降级策略**：

```java
catch (Exception e) {
    log.error("RAG估价异常, 降级到规则估价, categoryId={}", dto.getCategoryId(), e);
    return ruleEngine.estimate(dto);
}
```

- 所有异常（Milvus不可用、Embedding API超时、LLM API超时、JSON解析失败）统一降级到规则引擎
- **⚠️ 踩坑**：没有区分可恢复异常和不可恢复异常。例如，JSON解析失败可能是LLM输出了格式错误的响应，重试一次可能就成功了，但当前直接降级。建议增加重试机制（如Spring Retry）
- **⚠️ 踩坑**：降级后的结果`source="RULE"`，前端可以区分，但缺少告警机制。建议在降级时发送监控告警

---

## 三、模块整体架构分析

### 3.1 分层架构

```
┌─────────────────────────────────────────────────┐
│                  Controller 层                    │
│          PriceEstimationController                │
│          (HTTP入口, 参数接收, 结果包装)              │
├─────────────────────────────────────────────────┤
│                   Service 层                      │
│          AiPricingServiceImpl                     │
│     (核心业务编排: 防重复→RAG→降级)                  │
├────────┬──────────┬──────────┬──────────────────┤
│ Prompt │  Vector  │ Fallback │   Properties     │
│  层    │   层     │   层     │     层            │
│模板构建│Milvus封装│规则兜底  │  配置映射          │
├────────┴──────────┴──────────┴──────────────────┤
│                  Pipeline 层 (离线)               │
│     PricingDataPipeline                          │
│     (ETL: MySQL→聚合→Embedding→Milvus)           │
├─────────────────────────────────────────────────┤
│                  Infrastructure 层               │
│   MySQL(Mapper)  Redis  Milvus  DashScope(API)  │
└─────────────────────────────────────────────────┘
```

### 3.2 依赖关系图

```
Controller → Service → { PromptTemplate, VectorStore, RuleEngine }
                        VectorStore → { MilvusProperties, EmbeddingModel }
Pipeline → { PricingDataMapper, VectorStore, RedisTemplate }
Service → { CategoryMapper, RedisTemplate }
Config → { ChatModel } → ChatClient
```

**关键依赖特征**：
- **在线链路**：Controller → Service → VectorStore + PromptTemplate + ChatClient + RuleEngine
- **离线链路**：Pipeline → Mapper + VectorStore + RedisTemplate
- **在线链路不依赖Mapper**（除了查品类名称），实现了在线/离线解耦

### 3.3 核心数据流

```
[离线] MySQL成交记录 → Pipeline聚合 → Embedding API → Milvus向量库
                                                          ↓
[在线] 用户请求 → 防重复检查 → 构建查询文本 → Embedding → Milvus检索
                                                          ↓
         RAG结果 + 用户信息 → Prompt组装 → LLM推理 → JSON解析 → 估价结果
                                                          ↓ (异常)
                                              规则引擎兜底 → 估价结果
```

---

## 四、关键技术实现细节

### 4.1 RAG架构实现

本模块实现了标准的**RAG（Retrieval-Augmented Generation）**架构：

1. **离线索引阶段**：成交数据 → 聚合为知识片段 → Embedding为向量 → 存入Milvus
2. **在线检索阶段**：用户查询 → Embedding为向量 → Milvus相似度检索 → 获取Top-K知识片段
3. **增强生成阶段**：检索结果 + 用户信息 → 填入Prompt模板 → LLM生成估价结果

**RAG实现的关键设计决策**：
- Document的content字段格式精心设计，兼顾了Embedding质量和检索结果可读性
- metadata字段设计合理，支持按categoryId精确过滤
- Top-K默认为5，通过`xianzhuan.ai.pricing.rag-top-k`可配置

### 4.2 向量存储实现

- 使用Milvus 2.x Java SDK（`MilvusClientV2`），而非Spring AI的`MilvusVectorStore`自动装配
- 自行管理Milvus连接、Collection Schema、向量写入和检索
- **为什么不使用Spring AI的VectorStore抽象？** 可能原因：1) 需要自定义Schema（多metadata字段） 2) 需要精细控制检索参数 3) Spring AI的Milvus集成可能不够成熟

### 4.3 Fallback降级机制

降级触发条件（任一即触发）：
1. Milvus连接失败（`client == null`）
2. 向量检索异常
3. Embedding API异常
4. LLM API异常
5. LLM响应JSON解析失败

降级策略：返回基于成色折损规则的估价结果，置信度为LOW，来源标记为RULE。

**这是一个完整的Circuit Breaker思想的简化实现**，虽然没有使用Resilience4j等框架，但通过try-catch + 降级方法实现了相同的效果。

### 4.4 Prompt工程

- System Prompt：角色设定 + 5条估价原则 + 输出格式约束
- User Prompt：商品信息 + RAG检索结果（按相似度排序）
- 输出约束：严格JSON格式，5个字段，价格保留2位小数，explanation 50字以内

---

## 五、值得学习的亮点

1. **在线/离线解耦架构**：在线估价完全不查DB，数据由离线管线预Embedding到向量库。这保证了在线链路的高性能和高可用，是AI应用的最佳实践

2. **优雅降级设计**：从VectorStore到Service层，每一层都有异常处理和降级策略。Milvus不可用时返回空检索结果，RAG异常时降级到规则引擎，Redis异常时不影响主流程。这种**防御性编程**思维值得学习

3. **Document内容设计**：content字段`"iPhone 14 Pro Max 手机 九成新 原价7999 成交均价5999 成交15笔 折价率75%"`同时服务于Embedding和检索展示，一石二鸟

4. **增量均值计算**：`PricingKnowledgeSegment.accumulate()`使用在线算法计算均值，避免保存所有原始数据，内存效率高

5. **防重复缓存**：避免相同用户短时间内重复调用LLM API，节省成本，也避免了并发场景下的重复计算

6. **source字段溯源**：通过`source`字段区分AI/RULE来源，便于前端差异化展示和后端效果追踪

7. **配置外部化**：cron表达式、Top-K、去重TTL等关键参数都支持通过YAML配置覆盖，灵活性好

---

## 六、设计模式总结

| 设计模式 | 应用位置 | 说明 |
|---------|---------|------|
| 门面模式 | `PricingVectorStore` | 封装MilvusClientV2复杂API，暴露`upsert()`/`search()` |
| 策略模式 | `AiPricingServiceImpl` + `RuleBasedPricingEngine` | AI估价策略与规则估价策略可互换 |
| 模板方法 | `PricingPromptTemplate` | System Prompt固定，User Prompt动态填充 |
| 工厂方法 | `ChatClientConfig` | 通过`@Bean`创建ChatClient实例 |
| 建造者模式 | `PriceEstimationDTO`/`PriceEstimationVO` | 使用`@Builder`链式构建对象 |
| 降级模式 | `AiPricingServiceImpl.doEstimateWithRag()` | RAG异常时自动降级到规则引擎 |

---

## 七、改进建议

### 7.1 高优先级（生产必须）

| 问题 | 现状 | 建议 |
|------|------|------|
| 向量数据膨胀 | `upsert()`实际是`insert()`，每次管线执行追加写入 | 写入前先删除旧数据（`delete` by filter），或使用Milvus的`upsert()`API |
| 全量导出OOM | 每次管线全量查询所有成交记录 | 改为增量导出，基于`checkout_time`过滤 |
| Milvus无索引 | 创建Collection后未创建索引 | 创建HNSW或IVF_FLAT索引，设置合理的`metric_type`为`COSINE` |
| 缺少参数校验 | DTO无`@Valid`校验 | 添加JSR303注解 + Controller层`@Valid` |
| LLM响应解析脆弱 | `extractJson()`只能处理简单情况 | 使用Spring AI的`BeanOutputConverter`或Structured Output |

### 7.2 中优先级（质量提升）

| 问题 | 现状 | 建议 |
|------|------|------|
| Embedding无批量 | 每条Document单独调用Embedding API | 使用`embeddingModel.embed()`的批量接口，减少API调用次数 |
| 无重试机制 | LLM/Embedding API异常直接降级 | 引入Spring Retry，对可恢复异常进行重试 |
| 缺少相似度阈值 | 向量检索无最低相似度过滤 | 设置`searchParams`中的距离阈值，过滤不相关结果 |
| 品牌提取简陋 | 硬编码品牌列表做字符串匹配 | 引入LLM做NER，或使用更完善的品牌词典 |
| 无监控告警 | 降级时只打日志 | 接入Prometheus/Grafana，降级率超过阈值时告警 |

### 7.3 低优先级（架构演进）

| 问题 | 现状 | 建议 |
|------|------|------|
| 同步阻塞调用 | `chatClient.prompt().call()`同步等待 | 改为异步调用或流式响应，提升吞吐量 |
| 缺少对话记忆 | 每次估价独立，无上下文 | 引入ChatMemory，支持"为什么推荐这个价格？"等追问 |
| 无A/B测试能力 | 无法对比AI估价和规则估价的效果 | 增加实验分组机制，对比不同策略的成交转化率 |
| MilvusProperties.dimension类型 | String而非int | 改为int类型，利用Spring Boot配置绑定自动校验 |
| 字段注入 | 使用`@Autowired`字段注入 | 改为构造器注入，提升代码质量和可测试性 |

---

## 八、总结

本AI模块以**RAG架构**为核心，实现了一个完整的"离线数据管线 + 在线智能估价 + 规则兜底"的三层估价系统。架构设计思路清晰，在线/离线解耦、优雅降级、防御性编程等实践值得学习。主要的技术风险集中在：**向量数据膨胀（upsert语义缺失）**、**全量导出性能**、**LLM响应解析健壮性**三个方面，建议在生产上线前优先解决。