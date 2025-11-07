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
public class ShoppingCartDTO implements Serializable {

    private Long itemId;
    private String itemSpec;

}
