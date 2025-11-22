package com.xianzhuan.dto;

import com.xianzhuan.entity.ItemSpec;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
public class UsedItemDTO implements Serializable {

    private Long id;

    //商品名称
    private String name;

    //商品分类id
    private Long categoryId;

    //原价
    private BigDecimal originalPrice;

    //现价
    private BigDecimal currentPrice;

    //运费
    private BigDecimal shippingFee;

    //成色
    private String condition;

    //卖家ID
    private Long sellerId;

    //图片
    private String image;

    //销量
    private Integer salesCount;

    //描述信息
    private String description;

    //0 停售 1 起售
    private Integer status;

    //规格
    private List<ItemSpec> specs = new ArrayList<>();
}