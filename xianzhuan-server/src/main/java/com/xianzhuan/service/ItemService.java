package com.xianzhuan.service;

import com.xianzhuan.dto.UsedItemDTO;
import com.xianzhuan.dto.UsedItemPageQueryDTO;
import com.xianzhuan.entity.UsedItem;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.result.Result;
import com.xianzhuan.vo.UsedItemVO;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface ItemService {

    /**
     * 新增商品和对应的规格
     *
     * @param usedItemDTO
     */
    public Result<String> saveWithSpec(UsedItemDTO usedItemDTO);

    PageResult queryPage(UsedItemPageQueryDTO usedItemPageQueryDTO);

    Result<String> deleteBatch(List<Long> ids);

    UsedItemVO getById(Long id);

    Result<String> updateItem(UsedItemDTO usedItemDTO);

    List<UsedItem> list(Long categoryId);

    void startOrStop(Integer status, Long id);

    /**
     * 条件查询商品和规格
     *
     * @param usedItem
     * @return
     */
    List<UsedItemVO> listWithSpec(UsedItem usedItem);

}