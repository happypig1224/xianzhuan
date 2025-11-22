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
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FlashMessage implements Serializable {
    private Long userId;
    private Long dealId;
}