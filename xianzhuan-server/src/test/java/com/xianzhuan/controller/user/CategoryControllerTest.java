package com.xianzhuan.controller.user;

import com.xianzhuan.entity.Category;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户端 CategoryController 单元测试")
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    @DisplayName("查询分类 - 成功")
    void list_success() {
        // 1. 准备数据
        List<Category> categories = new ArrayList<>();
        when(categoryService.list(1)).thenReturn(categories);

        // 2. 调用方法
        Result<List<Category>> result = categoryController.list(1);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(categoryService).list(1);
    }

    @Test
    @DisplayName("查询分类 - type为null")
    void list_typeNull() {
        // 1. 准备数据
        List<Category> categories = new ArrayList<>();
        when(categoryService.list(null)).thenReturn(categories);

        // 2. 调用方法
        Result<List<Category>> result = categoryController.list(null);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(categoryService).list(null);
    }
}