package com.xianzhuan.ai.service;

import com.xianzhuan.ai.dto.request.PriceEstimationDTO;
import com.xianzhuan.ai.dto.response.PriceEstimationVO;

/**
 * AI智能定价服务
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface AiPricingService {
    PriceEstimationVO estimate(PriceEstimationDTO dto);
}