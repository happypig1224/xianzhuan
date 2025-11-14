package com.xianzhuan.mapper;

import com.xianzhuan.entity.FlashDeal;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Mapper
public interface FlashDealMapper {
    @Insert("INSERT INTO flash_deal (id, voucher_id, title, rules, pay_value, actual_value, status, stock, begin_time, end_time, create_time) " +
            "VALUES (#{id}, #{voucherId}, #{title}, #{rules}, #{payValue}, #{actualValue}, #{status}, #{stock}, #{beginTime}, #{endTime}, #{createTime})")
    int publishDeal(FlashDeal flashDeal);

    @Update("UPDATE flash_deal SET stock = stock - 1 WHERE id = #{dealId} AND stock > 0")
    int decreaseStock(Long dealId);

    @Select("SELECT * FROM flash_deal")
    List<FlashDeal> queryDeal();
}