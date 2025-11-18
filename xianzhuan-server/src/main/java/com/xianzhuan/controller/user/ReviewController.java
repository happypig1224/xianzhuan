package com.xianzhuan.controller.user;

import com.xianzhuan.dto.ItemReviewDTO;
import com.xianzhuan.entity.ItemReview;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.ReviewService;
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
@RestController
@RequestMapping("/user/review")
@Slf4j
@Tag(name = "C端-评价相关接口")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @Operation(summary = "提交评价")
    public Result submit(@RequestBody ItemReviewDTO reviewDTO) {
        // 步骤1: 记录提交评价日志
        log.info("提交评价：{}", reviewDTO);
        // 步骤2: 调用服务层保存评价
        reviewService.submitReview(reviewDTO);
        return Result.success();
    }

    @GetMapping("/item/{itemId}")
    @Operation(summary = "根据商品id查询评价列表")
    public Result<List<ItemReview>> listByItem(@PathVariable Long itemId) {
        // 步骤1: 记录查询日志
        log.info("根据商品id查询评价列表：{}", itemId);
        // 步骤2: 调用服务层查询评价
        List<ItemReview> list = reviewService.getByItemId(itemId);
        return Result.success(list);
    }

    @GetMapping("/seller")
    @Operation(summary = "查询当前卖家收到的评价列表")
    public Result<List<ItemReview>> listBySeller() {
        // 步骤1: 记录查询日志
        log.info("查询当前卖家收到的评价列表");
        // 步骤2: 调用服务层查询评价
        List<ItemReview> list = reviewService.getBySellerId(null);
        return Result.success(list);
    }
}
