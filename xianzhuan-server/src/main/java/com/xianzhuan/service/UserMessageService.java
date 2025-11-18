package com.xianzhuan.service;

import com.xianzhuan.dto.UserMessageDTO;
import com.xianzhuan.entity.UserMessage;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface UserMessageService {

    void send(UserMessageDTO userMessageDTO);

    List<UserMessage> listByUserId(Long userId);

    void markRead(Long id);

    Integer unreadCount(Long userId);
}
