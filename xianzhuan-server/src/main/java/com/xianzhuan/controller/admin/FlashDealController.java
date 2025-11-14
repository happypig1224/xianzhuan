package com.xianzhuan.controller.admin;

import com.xianzhuan.entity.FlashDeal;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.FlashDealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@RestController("adminFlashDealController")
@RequestMapping("/admin/flash")
public class FlashDealController {
    @Autowired
    private FlashDealService flashDealService;
    /**
     * 发布限时优惠
     * @param flashDeal
     * @return
     */
    @PostMapping("/publish")
    public Result<String> publishDeal(@RequestBody FlashDeal flashDeal) {
        // 用服务层发布优惠
        return flashDealService.publishDeal(flashDeal);
    }
}