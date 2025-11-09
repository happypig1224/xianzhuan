package com.xianzhuan.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
public class OrderStatisticsVO implements Serializable {
    //待接单数量
    private Integer toBeConfirmed;

    //待发货数量
    private Integer waitingShip;

    //已发货数量
    private Integer shipped;
}
