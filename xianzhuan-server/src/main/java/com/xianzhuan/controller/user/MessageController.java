package com.xianzhuan.controller.user;

import com.xianzhuan.context.BaseContext;
import com.xianzhuan.entity.UserMessage;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.UserMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@RestController
@RequestMapping("/user/message")
@Slf4j
@Tag(name = "C端-消息相关接口")
public class MessageController {

    @Autowired
    private UserMessageService userMessageService;

    @GetMapping("/list")
    @Operation(summary = "查询消息列表")
    public Result<List<UserMessage>> list() {
        // 步骤1: 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        // 步骤2: 调用服务层查询消息列表
        List<UserMessage> list = userMessageService.listByUserId(userId);
        return Result.success(list);
    }

    @PutMapping("/read/{id}")
    @Operation(summary = "标记消息已读")
    public Result markRead(@PathVariable Long id) {
        // 步骤1: 记录标记已读日志
        log.info("标记消息已读：{}", id);
        // 步骤2: 调用服务层标记已读
        userMessageService.markRead(id);
        return Result.success();
    }

    @GetMapping("/unread")
    @Operation(summary = "查询未读消息数量")
    public Result<Integer> unreadCount() {
        // 步骤1: 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        // 步骤2: 调用服务层查询未读数量
        Integer count = userMessageService.unreadCount(userId);
        return Result.success(count);
    }
}
