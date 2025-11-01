package com.xianzhuan.utils;

/**
 * Redis缓存常量定义
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public class RedisConstant {

    // 商品缓存Key
    public static final String ITEM_KEY = "item:cache:";

    // 购物车缓存Key
    public static final String SHOPPING_CART_KEY = "shopping:cart:";

    // 购物车联合索引缓存Key（用户ID + 商品ID）
    public static final String CART_INDEX_KEY = "cart:index:";

    // 缓存过期时间配置
    public static final long CACHE_EXPIRE_MINUTES = 30;
    public static final long CART_CACHE_EXPIRE_MINUTES = 30;

    public static final String FLASH_DEAL_KEY = "flash:deal:";
    public static final String FLASH_DEAL_LIMIT_KEY = "flash:deal:limit:";
    public static final String FLASH_USER_KEY = "flash:user:";
    public static final String FLASH_DEAL_BEGIN_KEY = "flash:deal:begin:";
    public static final String FLASH_LOCK_KEY = "flash:lock:";
    public static final String FLASH_ORDER_KEY = "flash:order:";
    public static final String FLASH_STOCK_KEY = "flash:stock";


    public static final String ORDER_TOKEN_KEY = "order:token:";
    public static final long ORDER_TOKEN_TTL_SECONDS = 1800L;
    public static final String CART_IDEMPOTENT_KEY = "cart:idempotent:";
    public static final long CART_IDEMPOTENT_TTL_SECONDS = 3L;

    // AI智能定价防重复请求Key
    public static final String AI_PRICING_DEDUP_KEY = "ai:pricing:req:";
}
