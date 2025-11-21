package com.xianzhuan.controller.user;

import com.xianzhuan.constant.StatusConstant;
import com.xianzhuan.entity.UsedItem;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.ItemService;
import com.xianzhuan.vo.UsedItemVO;
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
@DisplayName("用户端 ItemController 单元测试")
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    @Test
    @DisplayName("根据分类id查询物品 - 成功")
    void list_success() {
        // 1. 准备数据
        List<UsedItemVO> items = new ArrayList<>();
        when(itemService.listWithSpec(any(UsedItem.class))).thenReturn(items);

        // 2. 调用方法
        Result<List<UsedItemVO>> result = itemController.list(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(itemService).listWithSpec(any(UsedItem.class));
    }

    @Test
    @DisplayName("根据分类id查询物品 - categoryId为null")
    void list_categoryIdNull() {
        // 1. 准备数据
        List<UsedItemVO> items = new ArrayList<>();
        when(itemService.listWithSpec(any(UsedItem.class))).thenReturn(items);

        // 2. 调用方法
        Result<List<UsedItemVO>> result = itemController.list(null);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(itemService).listWithSpec(any(UsedItem.class));
    }
}