package com.xianzhuan.controller.user;

import com.xianzhuan.context.BaseContext;
import com.xianzhuan.entity.Address;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.AddressService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AddressController 单元测试")
class AddressControllerTest {

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(1L);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    @DisplayName("查询当前登录用户的所有地址信息 - 成功")
    void list_success() {
        // 1. 准备数据
        List<Address> addresses = new ArrayList<>();
        when(addressService.list(any(Address.class))).thenReturn(addresses);

        // 2. 调用方法
        Result<List<Address>> result = addressController.list();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(addressService).list(any(Address.class));
    }

    @Test
    @DisplayName("新增地址 - 成功")
    void save_success() {
        // 1. 准备数据
        Address address = new Address();
        doNothing().when(addressService).save(any(Address.class));

        // 2. 调用方法
        Result result = addressController.save(address);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(addressService).save(any(Address.class));
    }

    @Test
    @DisplayName("根据id查询地址 - 成功")
    void getById_success() {
        // 1. 准备数据
        Address address = new Address();
        when(addressService.getById(1L)).thenReturn(address);

        // 2. 调用方法
        Result<Address> result = addressController.getById(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(addressService).getById(1L);
    }

    @Test
    @DisplayName("根据id修改地址 - 成功")
    void update_success() {
        // 1. 准备数据
        Address address = new Address();
        doNothing().when(addressService).update(any(Address.class));

        // 2. 调用方法
        Result result = addressController.update(address);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(addressService).update(any(Address.class));
    }

    @Test
    @DisplayName("设置默认地址 - 成功")
    void setDefault_success() {
        // 1. 准备数据
        Address address = new Address();
        doNothing().when(addressService).setDefault(any(Address.class));

        // 2. 调用方法
        Result result = addressController.setDefault(address);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(addressService).setDefault(any(Address.class));
    }

    @Test
    @DisplayName("根据id删除地址 - 成功")
    void deleteById_success() {
        // 1. 准备数据
        doNothing().when(addressService).deleteById(1L);

        // 2. 调用方法
        Result result = addressController.deleteById(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(addressService).deleteById(1L);
    }

    @Test
    @DisplayName("查询默认地址 - 有默认地址")
    void getDefault_hasDefault() {
        // 1. 准备数据
        Address defaultAddress = new Address();
        defaultAddress.setIsDefault(1);
        List<Address> list = new ArrayList<>();
        list.add(defaultAddress);
        when(addressService.list(any(Address.class))).thenReturn(list);

        // 2. 调用方法
        Result<Address> result = addressController.getDefault();

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(addressService).list(any(Address.class));
    }

    @Test
    @DisplayName("查询默认地址 - 无默认地址")
    void getDefault_noDefault() {
        // 1. 准备数据
        List<Address> list = new ArrayList<>();
        when(addressService.list(any(Address.class))).thenReturn(list);

        // 2. 调用方法
        Result<Address> result = addressController.getDefault();

        // 3. 验证结果
        assertEquals(0, result.getCode());
        verify(addressService).list(any(Address.class));
    }
}