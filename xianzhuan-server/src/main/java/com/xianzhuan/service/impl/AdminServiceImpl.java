package com.xianzhuan.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xianzhuan.constant.MessageConstant;
import com.xianzhuan.constant.PasswordConstant;
import com.xianzhuan.constant.StatusConstant;
import com.xianzhuan.context.BaseContext;
import com.xianzhuan.dto.AdminDTO;
import com.xianzhuan.dto.AdminLoginDTO;
import com.xianzhuan.dto.AdminPageQueryDTO;
import com.xianzhuan.entity.Admin;
import com.xianzhuan.exception.AccountNotFoundException;
import com.xianzhuan.exception.PasswordErrorException;
import com.xianzhuan.mapper.AdminMapper;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.AdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 管理员登录
     *
     * @param adminLoginDTO
     * @return
     */
    public Admin login(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();
        // 步骤1：根据用户名查询数据库
        Admin admin = adminMapper.getByUsername(username);
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 步骤2：校验密码
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(admin.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        return admin;
    }

    /**
     * 新增管理员
     *
     * @param adminDTO
     */
    public Result save(AdminDTO adminDTO) throws SQLIntegrityConstraintViolationException {
        // 步骤1：构建管理员实体
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminDTO, admin);
        admin.setStatus(StatusConstant.ENABLE);
        admin.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        // 步骤2：保存管理员
        adminMapper.save(admin);
        return Result.success();
    }

    @Override
    public PageResult page(AdminPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<Admin> page = adminMapper.pageQuery(pageQueryDTO);
        long total = page.getTotal();
        List<Admin> result = page.getResult();
        return new PageResult(total, result);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Admin admin = Admin.builder()
                .id(id).status(status).build();
        adminMapper.update(admin);
    }

    @Override
    public Admin getById(Long id) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setPassword("********");
        return adminMapper.getById(admin);
    }

    @Override
    public void update(AdminDTO adminDTO) {
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminDTO, admin);
        admin.setUpdateTime(LocalDateTime.now());
        admin.setUpdateUser(BaseContext.getCurrentId());
        adminMapper.update(admin);
    }
}