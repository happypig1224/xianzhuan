package com.xianzhuan.service;

import com.xianzhuan.dto.AdminDTO;
import com.xianzhuan.dto.AdminLoginDTO;
import com.xianzhuan.dto.AdminPageQueryDTO;
import com.xianzhuan.entity.Admin;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.result.Result;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface AdminService {

    /**
     * 管理员登录
     *
     * @param adminLoginDTO
     * @return
     */
    Admin login(AdminLoginDTO adminLoginDTO);

    Result save(AdminDTO adminDTO) throws SQLIntegrityConstraintViolationException;

    PageResult page(AdminPageQueryDTO pageQueryDTO);

    void startOrStop(Integer status, Long id);

    Admin getById(Long id);

    void update(AdminDTO adminDTO);
}