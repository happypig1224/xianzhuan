package com.xianzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //用户ID
    private Long userId;

    //消息标题
    private String title;

    //消息内容
    private String content;

    //消息类型 0系统通知 1订单消息 2商品消息
    private Integer type;

    //是否已读 0未读 1已读
    private Integer isRead;

    //创建时间
    private LocalDateTime createTime;
}