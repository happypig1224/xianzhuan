package com.xianzhuan.controller.user;

import com.xianzhuan.context.BaseContext;
import com.xianzhuan.entity.UserMessage;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.UserMessageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageController 单元测试")
class MessageControllerTest {

    @Mock
    private UserMessageService userMessageService;

    @InjectMocks
    private MessageController messageController;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(1L);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    @DisplayName("查询消息列表 - 成功")
    void list_success() {
        // 1. 准备数据
        List<UserMessage> messages = new ArrayList<>();
        when(userMessageService.listByUserId(1L)).thenReturn(messages);

        // 2. 调用方法
        Result<List<UserMessage>> result = messageController.list();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(userMessageService).listByUserId(1L);
    }

    @Test
    @DisplayName("标记消息已读 - 成功")
    void markRead_success() {
        // 1. 准备数据
        doNothing().when(userMessageService).markRead(1L);

        // 2. 调用方法
        Result result = messageController.markRead(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(userMessageService).markRead(1L);
    }

    @Test
    @DisplayName("查询未读消息数量 - 成功")
    void unreadCount_success() {
        // 1. 准备数据
        when(userMessageService.unreadCount(1L)).thenReturn(5);

        // 2. 调用方法
        Result<Integer> result = messageController.unreadCount();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertEquals(5, result.getData());
        verify(userMessageService).unreadCount(1L);
    }
}