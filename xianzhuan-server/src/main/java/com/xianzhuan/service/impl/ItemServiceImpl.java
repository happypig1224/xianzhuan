package com.xianzhuan.service.impl;

import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xianzhuan.constant.StatusConstant;
import com.xianzhuan.context.BaseContext;
import com.xianzhuan.dto.UsedItemDTO;
import com.xianzhuan.dto.UsedItemPageQueryDTO;
import com.xianzhuan.dto.UserMessageDTO;
import com.xianzhuan.entity.UsedItem;
import com.xianzhuan.entity.ItemSpec;
import com.xianzhuan.mapper.ItemSpecMapper;
import com.xianzhuan.mapper.ItemMapper;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.ItemService;
import com.xianzhuan.service.UserMessageService;
import com.xianzhuan.utils.MemoryCacheUtil;
import com.xianzhuan.utils.RedisCacheUtils;
import com.xianzhuan.utils.RedisConstant;
import com.xianzhuan.vo.UsedItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    ItemSpecMapper itemSpecMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private MemoryCacheUtil localCache;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private RedisCacheUtils redisCacheUtils;

    /**
     * 新增商品和对应规格
     *
     * @param usedItemDTO
     */
    public Result<String> saveWithSpec(UsedItemDTO usedItemDTO) {
        Result<String> result = transactionTemplate.execute(status -> {
            try {
                log.info("编程式事务开启，新增商品名称{}", usedItemDTO.getName());
                UsedItem usedItem = new UsedItem();
                BeanUtils.copyProperties(usedItemDTO, usedItem);
                usedItem.setCreateTime(LocalDateTime.now());
                usedItem.setUpdateTime(LocalDateTime.now());
                usedItem.setCreateUser(BaseContext.getCurrentId());
                usedItem.setUpdateUser(BaseContext.getCurrentId());
                itemMapper.insert(usedItem);
                Long itemId = usedItem.getId();
                
                List<ItemSpec> specs = usedItemDTO.getSpecs();
                if (specs != null && !specs.isEmpty()) {
                    specs.forEach(spec -> {
                        spec.setItemId(itemId);
                    });
                    itemSpecMapper.insertBatch(specs);
                }
                log.info("添加商品{}成功", usedItemDTO.getName());
                return Result.success("添加商品成功!");
            } catch (BeansException e) {
                log.error("新增商品和对应规格失败", e);
                status.setRollbackOnly();
                return Result.error("添加失败");
            }
        });
        usedItemDTO.setStatus(StatusConstant.ENABLE);
        if (result.getCode() == 1) {
            String cacheKey = RedisConstant.ITEM_KEY + usedItemDTO.getCategoryId() + usedItemDTO.getStatus();
            // 雪崩防护：随机TTL（30分钟±5分钟）
            redisCacheUtils.setWithRandomExpire(cacheKey, usedItemDTO, 30, 5, TimeUnit.MINUTES);
            localCache.put(cacheKey, usedItemDTO, 30, TimeUnit.MINUTES);
        }
        return result;
    }

    @Override
    public PageResult queryPage(UsedItemPageQueryDTO usedItemPageQueryDTO) {
        PageHelper.startPage(usedItemPageQueryDTO.getPage(), usedItemPageQueryDTO.getPageSize());
        Page<UsedItemVO> usedItemVOPage = itemMapper.queryPage(usedItemPageQueryDTO);
        return new PageResult(usedItemVOPage.getTotal(), usedItemVOPage.getResult());
    }

    public Result<String> deleteBatch(List<Long> ids) {
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();
        Set<Long> categoryIds = new HashSet<>();

        for (Long id : ids) {
            try {
                UsedItem usedItem = itemMapper.getById(id);
                if (usedItem == null) {
                    failedIds.add(id);
                    continue;
                }
                if (usedItem.getStatus().equals(StatusConstant.ENABLE)) {
                    failedIds.add(id);
                    continue;
                }
                categoryIds.add(usedItem.getCategoryId());
            } catch (Exception e) {
                failedIds.add(id);
                log.warn("商品ID: {} 校验失败", id, e);
            }
        }

        Result<String> singleResult = null;
        for (Long id : ids) {
            if (failedIds.contains(id)) {
                continue;
            }

            singleResult = transactionTemplate.execute(status -> {
                try {
                    itemMapper.deleteByIds(id);
                    itemSpecMapper.deleteByItemId(id);
                    successIds.add(id);
                    return Result.success("删除成功");
                } catch (Exception e) {
                    status.setRollbackOnly();
                    failedIds.add(id);
                    log.error("商品ID: {} 删除失败", id, e);
                    return Result.error("删除失败");
                }
            });
        }

        if (!successIds.isEmpty() && singleResult.getCode() == 1) {
            for (Long categoryId : categoryIds) {
                try {
                    String cacheKey = RedisConstant.ITEM_KEY + categoryId + ":*";
                    stringRedisTemplate.delete(cacheKey);
                    cacheKey = RedisConstant.ITEM_KEY + categoryId + ":" + StatusConstant.ENABLE;
                    localCache.delete(cacheKey);
                    log.info("批量商品删除成功，失效分类ID: {} 的相关缓存", categoryId);
                } catch (Exception e) {
                    log.warn("缓存失效操作失败，分类ID: {}", categoryId, e);
                }
            }
        }

        if (failedIds.isEmpty()) {
            return Result.success("批量删除成功，共删除 " + successIds.size() + " 个商品");
        } else if (successIds.isEmpty()) {
            return Result.error("批量删除失败，所有商品删除失败");
        } else {
            return Result.success("部分删除成功，成功: " + successIds.size() + " 个，失败: " + failedIds.size() + " 个");
        }
    }

    @Override
    public UsedItemVO getById(Long id) {
        UsedItem usedItem = itemMapper.getById(id);
        List<ItemSpec> itemSpecs = itemSpecMapper.getByItemId(id);
        UsedItemVO usedItemVO = new UsedItemVO();
        BeanUtils.copyProperties(usedItem, usedItemVO);
        usedItemVO.setSpecs(itemSpecs);
        try {
            String viewsKey = RedisConstant.ITEM_KEY + "views:" + id;
            stringRedisTemplate.opsForValue().increment(viewsKey);
        } catch (Exception e) {
            log.warn("浏览量递增失败，商品ID: {}", id, e);
        }
        return usedItemVO;
    }

    public Result<String> updateItem(UsedItemDTO usedItemDTO) {
        Result<String> result = transactionTemplate.execute(status -> {
            try {
                log.info("开始编程式事务处理商品更新，商品ID: {}", usedItemDTO.getId());

                UsedItem existingItem = itemMapper.getById(usedItemDTO.getId());
                if (existingItem == null) {
                    throw new RuntimeException("商品不存在，ID: " + usedItemDTO.getId());
                }

                UsedItem usedItem = new UsedItem();
                BeanUtils.copyProperties(usedItemDTO, usedItem);
                usedItem.setUpdateTime(LocalDateTime.now());
                usedItem.setUpdateUser(BaseContext.getCurrentId());

                itemMapper.updateItem(usedItem);

                itemSpecMapper.deleteByItemId(usedItemDTO.getId());
                List<ItemSpec> specs = usedItemDTO.getSpecs();
                if (specs != null && !specs.isEmpty()) {
                    specs.forEach(spec -> {
                        spec.setItemId(usedItemDTO.getId());
                    });
                    itemSpecMapper.insertBatch(specs);
                }

                return Result.success("商品更新成功");
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("商品ID: {} 更新失败", usedItemDTO.getId(), e);
                return Result.error("更新失败");
            }
        });
        log.info("结束编程式事务处理商品更新，结果: {}", result);
        boolean flag = false;
        if (result.getCode() == 1) {
            try {
                UsedItem updatedItem = itemMapper.getById(usedItemDTO.getId());
                if (updatedItem != null) {
                    String cacheKey = RedisConstant.ITEM_KEY + updatedItem.getCategoryId() + ":*";
                    stringRedisTemplate.delete(cacheKey);
                    cacheKey = RedisConstant.ITEM_KEY + updatedItem.getCategoryId() + ":" + updatedItem.getStatus();
                    localCache.delete(cacheKey);
                    flag = true;
                }
            } catch (Exception e) {
                log.warn("缓存失效操作失败，商品ID: {}", usedItemDTO.getId(), e);
            }
        }
        return flag ? Result.success("商品更新成功") : Result.error("更新失败");
    }

    @Override
    public List<UsedItem> list(Long categoryId) {
        UsedItem usedItem = UsedItem.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return itemMapper.list(usedItem);
    }

    /**
     * 起售/停售商品
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        UsedItem usedItem = UsedItem.builder()
                .id(id)
                .status(status)
                .build();
        itemMapper.updateItem(usedItem);

        try {
            UsedItem updatedItem = itemMapper.getById(id);
            if (updatedItem != null) {
                String cacheKey = RedisConstant.ITEM_KEY + updatedItem.getCategoryId() + ":*";
                stringRedisTemplate.delete(cacheKey);
                log.info("商品状态更新，失效分类ID: {} 的相关缓存", updatedItem.getCategoryId());
            }
        } catch (Exception e) {
            log.warn("缓存失效操作失败，商品ID: {}", id, e);
        }

        try {
            UsedItem updatedItem = itemMapper.getById(id);
            if (updatedItem != null) {
                if (StatusConstant.ENABLE.equals(status)) {
                    UserMessageDTO msgDTO = UserMessageDTO.builder()
                            .userId(updatedItem.getSellerId())
                            .title("商品已上架")
                            .content("您的商品「" + updatedItem.getName() + "」已上架销售")
                            .type(2)
                            .build();
                    userMessageService.send(msgDTO);
                } else if (StatusConstant.DISABLE.equals(status)) {
                    UserMessageDTO msgDTO = UserMessageDTO.builder()
                            .userId(updatedItem.getSellerId())
                            .title("商品已下架")
                            .content("您的商品「" + updatedItem.getName() + "」已下架")
                            .type(2)
                            .build();
                    userMessageService.send(msgDTO);
                }
            }
        } catch (Exception e) {
            log.warn("发送商品状态变更站内消息失败，商品ID: {}", id, e);
        }
    }

    /**
     * 条件查询商品和规格
     *
     * @param usedItem
     * @return
     */
    public List<UsedItemVO> listWithSpec(UsedItem usedItem) {
        String cacheKey = RedisConstant.ITEM_KEY + usedItem.getCategoryId() + ":" + usedItem.getStatus();

        try {
            Object cache = localCache.get(cacheKey);
            if (cache != null) {
                return JSONUtil.toList(cache.toString(), UsedItemVO.class);
            }
        } catch (Exception e) {
            log.warn("本地缓存读取失败,降级Redis缓存, key: {}", cacheKey, e);
        }

        String json = redisCacheUtils.queryWithMutex(cacheKey, String.class, key -> {
            List<UsedItem> itemList = itemMapper.list(usedItem);
            if (itemList == null || itemList.isEmpty()) {
                return null;
            }
            List<UsedItemVO> voList = new ArrayList<>();
            for (UsedItem d : itemList) {
                UsedItemVO vo = new UsedItemVO();
                BeanUtils.copyProperties(d, vo);
                List<ItemSpec> specs = itemSpecMapper.getByItemId(d.getId());
                vo.setSpecs(specs);
                voList.add(vo);
            }
            return JSONUtil.toJsonStr(voList);
        }, 30, TimeUnit.MINUTES);

        if (json == null) {
            return new ArrayList<>();
        }

        List<UsedItemVO> usedItemVOList = JSONUtil.toList(json, UsedItemVO.class);

        try {
            localCache.put(cacheKey, json, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("本地缓存写入失败, key: {}", cacheKey, e);
        }

        return usedItemVOList;
    }
}