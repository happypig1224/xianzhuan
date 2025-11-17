package com.xianzhuan.service;

import com.xianzhuan.entity.LocalMessage;

/**
 * @Author 吴汇明
 * @School 绥化学院
 * @CreateTime
 */
public interface MessageHandlerService {
    void handleMessage(LocalMessage message);
    String getSupportedMessageType();
}
