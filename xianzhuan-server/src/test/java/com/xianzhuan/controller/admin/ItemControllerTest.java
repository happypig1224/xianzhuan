package com.xianzhuan.controller.admin;

import com.xianzhuan.dto.UsedItemDTO;
import com.xianzhuan.dto.UsedItemPageQueryDTO;
import com.xianzhuan.entity.UsedItem;
import com.xianzhuan.result.PageResult;
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
import java.util.Arrays;
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
@DisplayName("管理端 ItemController 单元测试")
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    @Test
    @DisplayName("新增物品 - 成功")
    void save_success() {
        // 1. 准备数据
        UsedItemDTO dto = new UsedItemDTO();
        when(itemService.saveWithSpec(any(UsedItemDTO.class))).thenReturn(Result.success());

        // 2. 调用方法
        Result result = itemController.save(dto);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(itemService).saveWithSpec(any(UsedItemDTO.class));
    }

    @Test
    @DisplayName("物品分页查询 - 成功")
    void page_success() {
        // 1. 准备数据
        UsedItemPageQueryDTO queryDTO = new UsedItemPageQueryDTO();
        PageResult pageResult = new PageResult(10L, new ArrayList<>());
        when(itemService.queryPage(any(UsedItemPageQueryDTO.class))).thenReturn(pageResult);

        // 2. 调用方法
        Result<PageResult> result = itemController.page(queryDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(itemService).queryPage(any(UsedItemPageQueryDTO.class));
    }

    @Test
    @DisplayName("物品批量删除 - 成功")
    void delete_success() {
        // 1. 准备数据
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(itemService.deleteBatch(anyList())).thenReturn(Result.success());

        // 2. 调用方法
        Result result = itemController.delete(ids);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(itemService).deleteBatch(anyList());
    }

    @Test
    @DisplayName("根据id查询物品 - 成功")
    void getById_success() {
        // 1. 准备数据
        UsedItemVO itemVO = new UsedItemVO();
        when(itemService.getById(1L)).thenReturn(itemVO);

        // 2. 调用方法
        Result<UsedItemVO> result = itemController.getById(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(itemService).getById(1L);
    }

    @Test
    @DisplayName("修改物品 - 成功")
    void update_success() {
        // 1. 准备数据
        UsedItemDTO dto = new UsedItemDTO();
        when(itemService.updateItem(any(UsedItemDTO.class))).thenReturn(Result.success());

        // 2. 调用方法
        Result result = itemController.update(dto);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(itemService).updateItem(any(UsedItemDTO.class));
    }

    @Test
    @DisplayName("根据分类id查询物品 - 成功")
    void list_success() {
        // 1. 准备数据
        List<UsedItem> items = new ArrayList<>();
        when(itemService.list(anyLong())).thenReturn(items);

        // 2. 调用方法
        Result<List<UsedItem>> result = itemController.list(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(itemService).list(1L);
    }

    @Test
    @DisplayName("起售或停售物品 - 成功")
    void startOrStop_success() {
        // 1. 准备数据
        doNothing().when(itemService).startOrStop(anyInt(), anyLong());

        // 2. 调用方法
        Result result = itemController.startOrStop(1, 1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(itemService).startOrStop(1, 1L);
    }
}