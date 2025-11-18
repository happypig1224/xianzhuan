package com.xianzhuan.service.impl;

import com.xianzhuan.dto.UserMessageDTO;
import com.xianzhuan.entity.UserMessage;
import com.xianzhuan.mapper.UserMessageMapper;
import com.xianzhuan.service.UserMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Service
@Slf4j
public class UserMessageServiceImpl implements UserMessageService {

    @Autowired
    private UserMessageMapper userMessageMapper;

    @Override
    public void send(UserMessageDTO userMessageDTO) {
        // 步骤1：构建消息实体
        UserMessage userMessage = new UserMessage();
        BeanUtils.copyProperties(userMessageDTO, userMessage);
        userMessage.setIsRead(0);
        userMessage.setCreateTime(LocalDateTime.now());
        // 步骤2：保存消息
        userMessageMapper.insert(userMessage);
        log.info("发送用户{}消息成功", userMessage.getUserId());
    }

    @Override
    public List<UserMessage> listByUserId(Long userId) {
        // 步骤1：根据用户ID查询消息列表
        return userMessageMapper.listByUserId(userId);
    }

    @Override
    public void markRead(Long id) {
        // 步骤1：标记消息为已读
        userMessageMapper.markAsRead(id);
        log.info("消息{}标记为已读", id);
    }

    @Override
    public Integer unreadCount(Long userId) {
        // 步骤1：统计未读消息数量
        return userMessageMapper.countUnread(userId);
    }
}
