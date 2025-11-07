package com.xianzhuan.service.impl;

import cn.hutool.json.JSONUtil;
import com.xianzhuan.context.BaseContext;
import com.xianzhuan.dto.ShoppingCartDTO;
import com.xianzhuan.entity.UsedItem;
import com.xianzhuan.entity.ShoppingCart;
import com.xianzhuan.mapper.ItemMapper;
import com.xianzhuan.mapper.ShoppingCartMapper;
import com.xianzhuan.service.ShoppingCartService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.xianzhuan.utils.RedisCacheUtils;

import static com.xianzhuan.utils.RedisConstant.SHOPPING_CART_KEY;
import static com.xianzhuan.utils.RedisConstant.CART_IDEMPOTENT_KEY;
import static com.xianzhuan.utils.RedisConstant.CART_IDEMPOTENT_TTL_SECONDS;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisCacheUtils redisCacheUtils;

    private void rebuildCartCache(Long userId) {
        stringRedisTemplate.delete(SHOPPING_CART_KEY + userId);
        try {
            List<ShoppingCart> fullList = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(userId).build()
            );
            // 雪崩防护：随机TTL（30分钟±3分钟）
            redisCacheUtils.setWithRandomExpire(SHOPPING_CART_KEY + userId, fullList, 30, 3, TimeUnit.MINUTES);
            log.info("购物车缓存同步重建完成，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("同步重建购物车缓存失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
        }
    }

    @Transactional
    public List<ShoppingCart> addShoppingCart(ShoppingCartDTO dto) {
        Long userId = BaseContext.getCurrentId();
        String idempotentKey = CART_IDEMPOTENT_KEY + userId + ":" + dto.getItemId() + ":add";
        try {
            Boolean isNew = stringRedisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", CART_IDEMPOTENT_TTL_SECONDS, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(isNew)) {
                return showShoppingCart();
            }
        } catch (Exception e) {
            log.warn("Redis不可用，购物车添加幂等校验降级放行，userId: {}", userId);
        }
        
        ShoppingCart query = ShoppingCart.builder()
                .userId(userId)
                .itemId(dto.getItemId())
                .build();

        List<ShoppingCart> exists = shoppingCartMapper.list(query);

        if (!exists.isEmpty()) {
            ShoppingCart cart = exists.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        } else {
            ShoppingCart newCart = new ShoppingCart();
            BeanUtils.copyProperties(dto, newCart);
            newCart.setUserId(userId);
            newCart.setNumber(1);
            newCart.setCreateTime(LocalDateTime.now());

            if (dto.getItemId() != null) {
                UsedItem usedItem = itemMapper.getById(dto.getItemId());
                newCart.setName(usedItem.getName());
                newCart.setImage(usedItem.getImage());
                newCart.setAmount(usedItem.getOriginalPrice());
            }
            int rows = shoppingCartMapper.insert(newCart);
            if (rows <= 0) {
                log.info("新增购物车项失败，用户ID: {}, 商品ID: {}", userId, dto.getItemId());
            } else {
                log.info("新增购物车项成功，用户ID: {}, 商品ID: {}", userId, dto.getItemId());
            }
        }
        stringRedisTemplate.delete(SHOPPING_CART_KEY + userId);
        return showShoppingCart();
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        String key = SHOPPING_CART_KEY + userId;

        // 穿透防护(空值缓存) + 雪崩防护(随机TTL)
        String json = redisCacheUtils.queryWithPassThrough(key, String.class, k -> {
            List<ShoppingCart> dbList = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(userId).build()
            );
            if (dbList == null || dbList.isEmpty()) {
                return null;
            }
            return JSONUtil.toJsonStr(dbList);
        }, 30, TimeUnit.MINUTES);

        if (json == null) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(json, ShoppingCart.class);
        } catch (Exception e) {
            log.warn("购物车缓存解析失败, key={}", key, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteCartById(userId);
        // 注释缓存删除
         String key = SHOPPING_CART_KEY + userId;
         stringRedisTemplate.delete(key);
    }

    @Override
    public List<ShoppingCart> subShopping(ShoppingCartDTO dto) {
        Long userId = BaseContext.getCurrentId();
        String idempotentKey = CART_IDEMPOTENT_KEY + userId + ":" + dto.getItemId() + ":sub";
        try {
            Boolean isNew = stringRedisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", CART_IDEMPOTENT_TTL_SECONDS, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(isNew)) {
                return showShoppingCart();
            }
        } catch (Exception e) {
            log.warn("Redis不可用，购物车扣减幂等校验降级放行，userId: {}", userId);
        }
        
        ShoppingCart query = ShoppingCart.builder()
                .userId(userId)
                .itemId(dto.getItemId())
                .build();

        List<ShoppingCart> exists = shoppingCartMapper.list(query);

        if (!exists.isEmpty()) {
            ShoppingCart cart = exists.get(0);
            if (cart.getNumber() > 1) {
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            } else {
                shoppingCartMapper.deleteCartOption(cart.getId());
            }
        }
        stringRedisTemplate.delete(SHOPPING_CART_KEY + userId);
        return showShoppingCart();
    }

    public void addShoppingCartByUserId(ShoppingCartDTO shoppingCartDTO, Long userId) {
        try {
            // 先设置用户 ID 到上下文
            BaseContext.setCurrentId(userId);
            log.info("addShoppingCartByUserId: 已设置 userId={}", userId);
            ShoppingCartService proxy = (ShoppingCartService) AopContext.currentProxy();
            proxy.addShoppingCart(shoppingCartDTO);
        } finally {
            // 清理上下文
            BaseContext.removeCurrentId();
        }
    }

}