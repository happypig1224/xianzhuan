package com.xianzhuan.ai.controller;

import com.xianzhuan.ai.dto.request.PriceEstimationDTO;
import com.xianzhuan.ai.dto.response.PriceEstimationVO;
import com.xianzhuan.ai.service.AiPricingService;
import com.xianzhuan.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI智能估价控制器
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@RestController
@RequestMapping("/user/priceEstimation")
@Slf4j
@Tag(name = "C端-AI智能估价接口")
public class PriceEstimationController {

    @Autowired
    private AiPricingService aiPricingService;

    /**
     * AI智能估价
     */
    @PostMapping("/estimate-price")
    @Operation(summary = "AI智能估价")
    public Result<PriceEstimationVO> estimatePrice(@RequestBody PriceEstimationDTO dto) {
        log.info("智能估价请求: categoryId={}, condition={}, originalPrice={}, name={}",
                dto.getCategoryId(), dto.getCondition(), dto.getOriginalPrice(), dto.getName());
        PriceEstimationVO vo = aiPricingService.estimate(dto);
        return Result.success(vo);
    }
}
