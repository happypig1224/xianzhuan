package com.xianzhuan.mapper;

import com.xianzhuan.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id=#{currentId}")
    void deleteCartById(Long currentId);

    void subShopping(ShoppingCart shoppingCart);

    void insertBatch(List<ShoppingCart> shoppingCartList);

    /**
     * 根据id删除购物车选项
     * @param id
     */
    @Delete("delete from shopping_cart where id=#{id}")
    void deleteCartOption(Long id);

     /**
     * 新增购物车项
     * @param newCart
     * @return
     */
    @Insert("insert into shopping_cart (name, user_id, item_id, item_spec, number, amount, image, create_time) " +
            " values (#{name},#{userId},#{itemId},#{itemSpec},#{number},#{amount},#{image},#{createTime})")
    int insert(ShoppingCart newCart);
}
