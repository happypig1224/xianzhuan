package com.xianzhuan.controller.user;

import com.xianzhuan.entity.FlashOrder;
import com.xianzhuan.entity.FlashDeal;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.FlashDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@RestController("userFlashDealController")
@RequestMapping("/user/flash")
@Tag(name = "C端-限时优惠接口")
@Slf4j
public class FlashDealController {
    @Autowired
    private FlashDealService flashDealService;

    /**
     * 限时抢购
     * @param voucherId
     * @return
     */
    @GetMapping("/flash/{voucherId}")
    @Operation(summary = "限时抢购")
    public Result<String> flashBuy(@PathVariable("voucherId") Long voucherId) {
        // 调用服务层执行限时抢购
        return flashDealService.flashBuy(voucherId);
    }

    /**
     * 查询限时优惠
     * @return
     */
    @GetMapping("/queryDeal")
    public Result<List<FlashDeal>> queryDeal() {
        log.info("查询限时优惠");
        return flashDealService.queryDeal();
    }

    /**
     * 查询用户优惠列表
     * @return
     */
    @GetMapping("/myVoucherList")
    public Result<List<FlashOrder>> myVoucherList() {
        log.info("查询用户优惠列表");
        return flashDealService.myDealList();
    }
}