package com.xianzhuan.mapper;

import com.xianzhuan.entity.ItemReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Mapper
public interface ReviewMapper {

    void insert(ItemReview itemReview);

    @Select("select * from item_review where id = #{id}")
    ItemReview getById(Long id);

    List<ItemReview> listByItemId(Long itemId);

    List<ItemReview> listBySellerId(Long sellerId);
}
