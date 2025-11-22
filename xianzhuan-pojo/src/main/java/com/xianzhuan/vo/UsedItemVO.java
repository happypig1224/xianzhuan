package com.xianzhuan.vo;

import com.xianzhuan.entity.ItemSpec;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedItemVO implements Serializable {

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

    //浏览量
    private Integer views;

    //图片
    private String image;

    //描述信息
    private String description;

    //销量
    private Integer salesCount;

    //0 停售 1 起售
    private Integer status;

    //更新时间
    private LocalDateTime updateTime;

    //分类名称
    private String categoryName;

    //商品关联的规格
    private List<ItemSpec> specs = new ArrayList<>();
}