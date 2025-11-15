-- KEYS[1]: 优惠库存 key  (flash:deal:{voucherId})
-- KEYS[2]: 用户购买记录 key (flash:user:{userId}:{voucherId})
-- 返回值: 1=成功, -1=库存不足, -2=已领取过
-- 优化: SET NX EX 合并 SETNX+EXPIRE 为一条命令，Lua内从4条命令减少到3条

local stock_key = KEYS[1]
local user_key = KEYS[2]

local stock = redis.call('get', stock_key)
if not stock or tonumber(stock) <= 0 then
    return -1
end

local bought = redis.call('set', user_key, '1', 'NX', 'EX', 86400)
if not bought then
    return -2
end

redis.call('decr', stock_key)
return 1