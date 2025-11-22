package com.xianzhuan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageDTO implements Serializable {

    private Long userId;

    private String title;

    private String content;

    //消息类型 0系统通知 1订单消息 2商品消息
    private Integer type;
}