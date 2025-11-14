package com.xianzhuan.mapper;

import com.xianzhuan.entity.FlashOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Mapper
public interface FlashOrderMapper {

    /**
     * 保存闪购订单
     */
    @Insert("insert into flash_order(user_id,deal_id,order_time,status,create_time) values(#{userId},#{dealId},#{orderTime},#{status},#{createTime})")
    int save(FlashOrder flashOrder);

    /**
     * 查询用户闪购订单列表
     */
    @Select("select * from flash_order where user_id = #{userId}")
    List<FlashOrder> selectList(Long userId);
}