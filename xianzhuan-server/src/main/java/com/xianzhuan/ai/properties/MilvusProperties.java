package com.xianzhuan.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Component
@Data
@ConfigurationProperties(prefix = "xianzhuan.ai.milvus")
public class MilvusProperties {
    private String host;
    private int port;
    private String collectionName;
    private String dimension;
}
