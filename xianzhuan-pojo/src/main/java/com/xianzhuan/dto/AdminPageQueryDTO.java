package com.xianzhuan.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
public class AdminPageQueryDTO implements Serializable {

    //管理员姓名
    private String name;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}