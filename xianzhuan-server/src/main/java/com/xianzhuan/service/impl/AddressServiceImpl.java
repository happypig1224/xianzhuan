package com.xianzhuan.service.impl;

import com.xianzhuan.context.BaseContext;
import com.xianzhuan.entity.Address;
import com.xianzhuan.mapper.AddressMapper;
import com.xianzhuan.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Service
@Slf4j
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressMapper addressMapper;

    /**
     * 条件查询
     *
     * @param address
     * @return
     */
    public List<Address> list(Address address) {
        return addressMapper.list(address);
    }

    /**
     * 新增地址
     *
     * @param address
     */
    public void save(Address address) {
        // 步骤1：设置用户ID和默认值
        address.setUserId(BaseContext.getCurrentId());
        address.setIsDefault(0);
        // 步骤2：插入地址
        addressMapper.insert(address);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public Address getById(Long id) {
        Address address = addressMapper.getById(id);
        return address;
    }

    /**
     * 根据id修改地址
     *
     * @param address
     */
    public void update(Address address) {
        addressMapper.update(address);
    }

    /**
     * 设置默认地址
     *
     * @param address
     */
    @Transactional
    public void setDefault(Address address) {
        // 步骤1：将当前用户的所有地址修改为非默认地址
        address.setIsDefault(0);
        address.setUserId(BaseContext.getCurrentId());
        addressMapper.updateIsDefaultByUserId(address);

        // 步骤2：将当前地址改为默认地址
        address.setIsDefault(1);
        addressMapper.update(address);
    }

    /**
     * 根据id删除地址
     *
     * @param id
     */
    public void deleteById(Long id) {
        addressMapper.deleteById(id);
    }

}