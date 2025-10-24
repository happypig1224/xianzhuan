package com.xianzhuan.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
public class AdminDTO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}