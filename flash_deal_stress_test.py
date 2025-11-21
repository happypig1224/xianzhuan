"""
优惠券秒杀压力测试 - 聚焦 QPS、超卖、稳定性
作者: Wu, Hui Ming
说明: 通过 Nginx 压测秒杀接口，验证 QPS、超卖、稳定性
"""
import requests
import threading
import time
import json
from collections import defaultdict
from datetime import datetime

# ==================== 配置区 ====================
CONFIG = {
    "base_url": "http://localhost",  # Nginx 地址（端口 80）
    "deal_id": 1780812075171,        # 限时优惠 ID (flash_deal 表的 id)
    "initial_stock": 100,            # 初始库存
}

# 测试阶段配置（梯度加压 - 库存100+限购1优化版）
# 关键：并发用户必须远大于库存，才能测试超卖防护
TEST_PHASES = [
    {"name": "基准测试", "users": 100, "duration": 30},   # 1:1 竞争
    {"name": "负载测试", "users": 300, "duration": 60},   # 3:1 竞争
    {"name": "压力测试", "users": 500, "duration": 60},   # 5:1 竞争
    {"name": "极限测试", "users": 1000, "duration": 30},  # 10:1 竞争
]

# ==================== 结果收集器 ====================
class TestMetrics:
    def __init__(self):
        self.lock = threading.Lock()
        self.total_requests = 0
        self.success_requests = 0
        self.fail_requests = 0
        self.oversold_requests = 0  # 抢购成功数
        self.response_times = []
        self.error_types = defaultdict(int)
        self.status_codes = defaultdict(int)
        self.start_time = None
        self.end_time = None
    
    def record(self, success, response_time, status_code, error_msg=None, is_flash_success=False):
        with self.lock:
            self.total_requests += 1
            if success:
                self.success_requests += 1
                if is_flash_success:
                    self.oversold_requests += 1
            else:
                self.fail_requests += 1
            self.response_times.append(response_time)
            self.status_codes[status_code] += 1
            if error_msg:
                self.error_types[error_msg] += 1
    
    def calculate_qps(self):
        duration = self.end_time - self.start_time
        return self.total_requests / duration if duration > 0 else 0
    
    def calculate_percentiles(self):
        if not self.response_times:
            return {"avg": 0, "p50": 0, "p90": 0, "p95": 0, "p99": 0}
        
        sorted_times = sorted(self.response_times)
        n = len(sorted_times)
        return {
            "avg": sum(sorted_times) / n,
            "p50": sorted_times[n // 2],
            "p90": sorted_times[int(n * 0.9)],
            "p95": sorted_times[int(n * 0.95)],
            "p99": sorted_times[int(n * 0.99)],
        }
    
    def print_report(self, phase_name):
        qps = self.calculate_qps()
        percentiles = self.calculate_percentiles()
        success_rate = (self.success_requests / self.total_requests * 100) if self.total_requests > 0 else 0
        
        print(f"\n{'='*60}")
        print(f"📊 [{phase_name}] 测试报告")
        print(f"{'='*60}")
        print(f"⏱️  持续时间: {self.end_time - self.start_time:.2f} 秒")
        print(f"📈 QPS: {qps:.2f}")
        print(f"✅ 成功率: {success_rate:.2f}% ({self.success_requests}/{self.total_requests})")
        print(f"❌ 失败数: {self.fail_requests}")
        print(f"🎯 抢购成功数: {self.oversold_requests}")
        print(f"\n⏰ 响应时间:")
        print(f"   平均: {percentiles['avg']*1000:.2f} ms")
        print(f"   P50:  {percentiles['p50']*1000:.2f} ms")
        print(f"   P90:  {percentiles['p90']*1000:.2f} ms")
        print(f"   P95:  {percentiles['p95']*1000:.2f} ms")
        print(f"   P99:  {percentiles['p99']*1000:.2f} ms")
        
        if self.error_types:
            print(f"\n⚠️  错误分布 (Top 10):")
            for error, count in sorted(self.error_types.items(), key=lambda x: -x[1])[:10]:
                print(f"   {error}: {count}")
        
        print(f"{'='*60}\n")


# ==================== 压测工作线程 ====================
def worker(user_id, metrics: TestMetrics, stop_event: threading.Event):
    """并发请求工作线程"""
    headers = {
        "Content-Type": "application/json",
        "userId": str(user_id),  # 直接从请求头传 userId
    }
    
    url = f"{CONFIG['base_url']}/user/flash/flash/{CONFIG['deal_id']}"
    
    while not stop_event.is_set():
        try:
            start = time.time()
            resp = requests.get(url, headers=headers, timeout=5)
            elapsed = time.time() - start
            
            try:
                data = resp.json()
                code = data.get("code")
                msg = data.get("msg", "")
                
                # HTTP 200 且业务 code 200 才算成功
                is_http_success = resp.status_code == 200
                is_business_success = code == 200
                is_flash_success = is_http_success and is_business_success and "抢购成功" in msg
                
                if is_flash_success:
                    metrics.record(True, elapsed, resp.status_code, is_flash_success=True)
                elif is_http_success:
                    # HTTP 成功但业务失败（库存不足、已领取等）
                    metrics.record(True, elapsed, resp.status_code, error_msg=msg)
                else:
                    metrics.record(False, elapsed, resp.status_code, error_msg=msg)
                    
            except Exception as e:
                metrics.record(False, elapsed, resp.status_code, error_msg=f"解析失败: {str(e)}")
                
        except requests.exceptions.Timeout:
            metrics.record(False, 5.0, 0, error_msg="请求超时")
        except requests.exceptions.ConnectionError:
            metrics.record(False, 0.0, 0, error_msg="连接失败")
        except Exception as e:
            metrics.record(False, 0.0, 0, error_msg=str(e))


# ==================== 超卖验证 ====================
def verify_no_oversold():
    """
    验证是否超卖
    需要手动执行 SQL 查询验证
    """
    print(f"\n{'='*60}")
    print(f"🔍 超卖验证")
    print(f"{'='*60}")
    print(f"初始库存: {CONFIG['initial_stock']}")
    print(f"\n请执行以下 SQL 验证:")
    print(f"1. 查询剩余库存:")
    print(f"   SELECT stock FROM flash_deal WHERE id = {CONFIG['deal_id']};")
    print(f"\n2. 查询订单总数:")
    print(f"   SELECT COUNT(*) FROM flash_order WHERE deal_id = {CONFIG['deal_id']};")
    print(f"\n3. 验证规则:")
    print(f"   ✓ 订单总数 <= {CONFIG['initial_stock']} (初始库存)")
    print(f"   ✓ 剩余库存 >= 0")
    print(f"   ✓ 订单总数 + 剩余库存 = {CONFIG['initial_stock']}")
    print(f"\n4. 查询 Redis 库存:")
    print(f"   redis-cli GET 'flash:deal:{CONFIG['voucher_id']}'")
    print(f"   验证: Redis 库存 == 数据库库存")
    print(f"{'='*60}\n")


# ==================== 主测试流程 ====================
def run_phase(phase_config):
    """执行单个测试阶段"""
    phase_name = phase_config["name"]
    num_users = phase_config["users"]
    duration = phase_config["duration"]
    
    print(f"\n{'='*60}")
    print(f"🚀 开始 [{phase_name}]")
    print(f"   并发用户: {num_users}, 持续时间: {duration}秒")
    print(f"{'='*60}")
    
    metrics = TestMetrics()
    stop_event = threading.Event()
    threads = []
    
    metrics.start_time = time.time()
    
    # 梯度启动线程（25% 时间用于启动）
    ramp_up = min(5, duration / 4)
    for i in range(num_users):
        t = threading.Thread(target=worker, args=(i, metrics, stop_event))
        t.daemon = True
        threads.append(t)
        t.start()
        time.sleep(ramp_up / num_users)
    
    print(f"✅ 所有用户已启动，测试进行中...")
    
    # 持续运行
    time.sleep(duration)
    
    # 停止所有线程
    stop_event.set()
    metrics.end_time = time.time()
    
    # 等待线程结束
    for t in threads:
        t.join(timeout=3)
    
    # 打印报告
    metrics.print_report(phase_name)
    
    return metrics


def main():
    print(f"\n{'='*60}")
    print(f"🎯 优惠券秒杀压力测试")
    print(f"   关注指标: QPS、超卖、稳定性")
    print(f"   限时优惠ID: {CONFIG['deal_id']}")
    print(f"   初始库存: {CONFIG['initial_stock']}")
    print(f"   Nginx 地址: {CONFIG['base_url']}")
    print(f"{'='*60}")
    
    print("\n✅ 请确认服务已启动:")
    print(f"   1. Nginx 已启动")
    print(f"   2. Spring Boot 已启动")
    print(f"   3. Redis 已启动")
    print(f"   4. MySQL 已启动")
    print(f"   5. RocketMQ 已启动")
    print(f"   6. 测试数据已准备 (deal_id={CONFIG['deal_id']}, stock={CONFIG['initial_stock']})")
    print(f"\n🚀 3秒后自动开始测试...")
    time.sleep(3)
    
    all_metrics = []
    
    # 执行各个测试阶段
    for phase in TEST_PHASES:
        metrics = run_phase(phase)
        all_metrics.append(metrics)
        
        # 阶段间休息
        print("⏸️  休息 10 秒，准备下一阶段...")
        time.sleep(10)
    
    # 汇总报告
    print(f"\n{'='*60}")
    print(f"📊 汇总报告")
    print(f"{'='*60}")
    total_flash_success = 0
    for idx, m in enumerate(all_metrics):
        qps = m.calculate_qps()
        success_rate = m.success_requests / m.total_requests * 100 if m.total_requests > 0 else 0
        total_flash_success += m.oversold_requests
        print(f"阶段 {idx+1} ({m.calculate_qps():.2f} QPS, {success_rate:.2f}% 成功率, {m.oversold_requests} 次抢购成功)")
    
    print(f"\n总抢购成功次数: {total_flash_success}")
    print(f"初始库存: {CONFIG['initial_stock']}")
    
    if total_flash_success > CONFIG['initial_stock']:
        print(f"❌ 警告: 抢购成功次数 ({total_flash_success}) > 初始库存 ({CONFIG['initial_stock']})")
        print(f"   可能存在超卖问题！")
    else:
        print(f"✅ 抢购成功次数未超过库存")
    
    # 超卖验证
    verify_no_oversold()
    
    print(f"\n{'='*60}")
    print(f"✅ 测试完成！")
    print(f"{'='*60}\n")


if __name__ == "__main__":
    main()