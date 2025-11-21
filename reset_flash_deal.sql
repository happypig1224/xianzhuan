-- 重置秒杀测试数据
-- 执行前请确认 voucher_id = 10001 存在

-- 1. 重置库存为 100
UPDATE flash_deal SET stock = 100 WHERE voucher_id = 10001;

-- 2. 清空历史订单（可选，如果要保留历史数据请注释掉）
DELETE FROM flash_order WHERE deal_id = (SELECT id FROM flash_deal WHERE voucher_id = 10001);

-- 3. 确认数据
SELECT id, voucher_id, title, stock, status, begin_time, end_time 
FROM flash_deal 
WHERE voucher_id = 10001;

-- 4. 确认订单数
SELECT COUNT(*) as order_count 
FROM flash_order 
WHERE deal_id = (SELECT id FROM flash_deal WHERE voucher_id = 10001);