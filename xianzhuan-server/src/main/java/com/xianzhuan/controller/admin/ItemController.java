package com.xianzhuan.controller.admin;

import com.xianzhuan.dto.UsedItemDTO;
import com.xianzhuan.dto.UsedItemPageQueryDTO;
import com.xianzhuan.entity.UsedItem;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.ItemService;
import com.xianzhuan.vo.UsedItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@RestController
@RequestMapping("/admin/item")
@Tag(name = "物品相关接口")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping
    @Operation(summary = "新增物品")
    public Result save(@RequestBody UsedItemDTO usedItemDTO) {
        // 步骤1: 记录新增物品日志
        log.info("新增物品:{}", usedItemDTO);
        // 步骤2: 调用服务层保存物品及规格
        itemService.saveWithSpec(usedItemDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "物品分页查询")
    public Result<PageResult> page(UsedItemPageQueryDTO usedItemPageQueryDTO) {
        log.info("物品分页查询:{}", usedItemPageQueryDTO);
        PageResult pageResult = itemService.queryPage(usedItemPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 物品批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @Operation(summary = "物品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("物品批量删除：{}", ids);
        return itemService.deleteBatch(ids);
    }

    @Operation(summary = "根据id查询物品")
    @GetMapping("/{id}")
    public Result<UsedItemVO> getById(@PathVariable Long id) {
        log.info("根据id查询物品:{}", id);
        UsedItemVO usedItemVO = itemService.getById(id);
        return Result.success(usedItemVO);
    }

    @PutMapping
    @Operation(summary = "修改物品")
    public Result update(@RequestBody UsedItemDTO usedItemDTO){
        // 步骤1: 调用服务层更新物品
        itemService.updateItem(usedItemDTO);
        return Result.success();
    }
    /**
     * 根据分类id查询物品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "根据分类id查询物品")
    public Result<List<UsedItem>> list(Long categoryId){
        List<UsedItem> list = itemService.list(categoryId);
        return Result.success(list);
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "起售或停售物品")
    public Result startOrStop(@PathVariable Integer status,Long id){
        itemService.startOrStop(status,id);
        return Result.success();
    }
}