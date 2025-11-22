package com.xianzhuan.vo;

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
public class ItemSpecVO implements Serializable {

    //商品名称
    private String name;

    //份数
    private Integer copies;

    //商品图片
    private String image;

    //商品描述
    private String description;
}