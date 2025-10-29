package com.xianzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品规格
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //商品id
    private Long itemId;

    //规格名称
    private String name;

    //规格数据list
    private String value;

}