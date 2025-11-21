package com.xianzhuan.controller.user;

import com.xianzhuan.dto.ShoppingCartDTO;
import com.xianzhuan.entity.ShoppingCart;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.ShoppingCartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShoppingCartController 单元测试")
class ShoppingCartControllerTest {

    @Mock
    private ShoppingCartService shoppingCartService;

    @InjectMocks
    private ShoppingCartController shoppingCartController;

    @Test
    @DisplayName("添加购物车 - 成功")
    void add_success() {
        // 1. 准备数据
        ShoppingCartDTO dto = new ShoppingCartDTO();
        List<ShoppingCart> cartList = new ArrayList<>();
        when(shoppingCartService.addShoppingCart(any(ShoppingCartDTO.class))).thenReturn(cartList);

        // 2. 调用方法
        Result<List<ShoppingCart>> result = shoppingCartController.add(dto);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(shoppingCartService).addShoppingCart(any(ShoppingCartDTO.class));
    }

    @Test
    @DisplayName("查看购物车列表 - 成功")
    void list_success() {
        // 1. 准备数据
        List<ShoppingCart> cartList = new ArrayList<>();
        when(shoppingCartService.showShoppingCart()).thenReturn(cartList);

        // 2. 调用方法
        Result<List<ShoppingCart>> result = shoppingCartController.list();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(shoppingCartService).showShoppingCart();
    }

    @Test
    @DisplayName("清空购物车商品 - 成功")
    void clean_success() {
        // 1. 准备数据
        doNothing().when(shoppingCartService).cleanShoppingCart();

        // 2. 调用方法
        Result<String> result = shoppingCartController.clean();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(shoppingCartService).cleanShoppingCart();
    }

    @Test
    @DisplayName("购物车商品扣减 - 成功")
    void sub_success() {
        // 1. 准备数据
        ShoppingCartDTO dto = new ShoppingCartDTO();
        List<ShoppingCart> cartList = new ArrayList<>();
        when(shoppingCartService.subShopping(any(ShoppingCartDTO.class))).thenReturn(cartList);

        // 2. 调用方法
        Result<List<ShoppingCart>> result = shoppingCartController.sub(dto);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(shoppingCartService).subShopping(any(ShoppingCartDTO.class));
    }
}