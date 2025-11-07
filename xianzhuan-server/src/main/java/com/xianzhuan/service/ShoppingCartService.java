package com.xianzhuan.service;

import com.xianzhuan.dto.ShoppingCartDTO;
import com.xianzhuan.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    List<ShoppingCart> addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();

    void cleanShoppingCart();

    List<ShoppingCart> subShopping(ShoppingCartDTO shoppingCartDTO);

    void addShoppingCartByUserId(ShoppingCartDTO shoppingCartDTO, Long userId);
}
