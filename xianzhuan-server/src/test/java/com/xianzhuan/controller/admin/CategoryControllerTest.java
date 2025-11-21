package com.xianzhuan.controller.admin;

import com.xianzhuan.dto.CategoryDTO;
import com.xianzhuan.dto.CategoryPageQueryDTO;
import com.xianzhuan.result.PageResult;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("管理端 CategoryController 单元测试")
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    @DisplayName("分页查询分类 - 成功")
    void page_success() {
        // 1. 准备数据
        CategoryPageQueryDTO queryDTO = new CategoryPageQueryDTO();
        PageResult pageResult = new PageResult(10L, new ArrayList<>());
        when(categoryService.page(any(CategoryPageQueryDTO.class))).thenReturn(pageResult);

        // 2. 调用方法
        Result<PageResult> result = categoryController.page(queryDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(categoryService).page(any(CategoryPageQueryDTO.class));
    }

    @Test
    @DisplayName("新增菜品分类 - 成功")
    void save_success() {
        // 1. 准备数据
        CategoryDTO categoryDTO = new CategoryDTO();
        doNothing().when(categoryService).save(any(CategoryDTO.class));

        // 2. 调用方法
        Result result = categoryController.save(categoryDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(categoryService).save(any(CategoryDTO.class));
    }

    @Test
    @DisplayName("修改分类 - 成功")
    void update_success() {
        // 1. 准备数据
        CategoryDTO categoryDTO = new CategoryDTO();
        doNothing().when(categoryService).update(any(CategoryDTO.class));

        // 2. 调用方法
        Result result = categoryController.update(categoryDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(categoryService).update(any(CategoryDTO.class));
    }

    @Test
    @DisplayName("删除分类 - 成功")
    void delete_success() {
        // 1. 准备数据
        doNothing().when(categoryService).deleteById(anyLong());

        // 2. 调用方法
        Result result = categoryController.delete(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(categoryService).deleteById(1L);
    }

    @Test
    @DisplayName("禁用或启用分类 - 成功")
    void startOrStop_success() {
        // 1. 准备数据
        doNothing().when(categoryService).startOrStop(anyInt(), anyLong());

        // 2. 调用方法
        Result result = categoryController.startOrStop(1, 1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(categoryService).startOrStop(1, 1L);
    }

    @Test
    @DisplayName("查询分类列表 - 成功")
    void list_success() {
        // 1. 准备数据
        List<CategoryDTO> categories = new ArrayList<>();
        when(categoryService.selectAll()).thenReturn(categories);

        // 2. 调用方法
        Result<List<CategoryDTO>> result = categoryController.list(1);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(categoryService).selectAll();
    }
}