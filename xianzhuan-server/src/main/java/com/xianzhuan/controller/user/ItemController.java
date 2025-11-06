package com.xianzhuan.controller.user;

import com.xianzhuan.constant.StatusConstant;
import com.xianzhuan.entity.UsedItem;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.ItemService;
import com.xianzhuan.vo.UsedItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@RestController("userItemController")
@RequestMapping("/user/item")
@Slf4j
@Tag(name = "C端-物品浏览接口")
public class ItemController {
    @Autowired
    private ItemService itemService;

    /**
     * 根据分类id查询物品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "根据分类id查询物品")
    public Result<List<UsedItemVO>> list(Long categoryId) {
        // 步骤1: 构建查询条件
        UsedItem usedItem = new UsedItem();
        usedItem.setCategoryId(categoryId);
        usedItem.setStatus(StatusConstant.ENABLE);
        // 步骤2: 查询起售中的物品及规格
        List<UsedItemVO> list = itemService.listWithSpec(usedItem);

        return Result.success(list);
    }

}