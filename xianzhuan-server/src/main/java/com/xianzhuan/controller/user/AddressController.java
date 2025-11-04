package com.xianzhuan.controller.user;

import com.xianzhuan.context.BaseContext;
import com.xianzhuan.entity.Address;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@RestController
@RequestMapping("/user/address")
@Tag(name = "C端地址接口")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 查询当前登录用户的所有地址信息
     *
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "查询当前登录用户的所有地址信息")
    public Result<List<Address>> list() {
        // 构建查询条件
        Address address = new Address();
        address.setUserId(BaseContext.getCurrentId());
        // 查询地址列表
        List<Address> list = addressService.list(address);
        return Result.success(list);
    }

    /**
     * 新增地址
     *
     * @param address
     * @return
     */
    @PostMapping
    @Operation(summary = "新增地址")
    public Result save(@RequestBody Address address) {
        addressService.save(address);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询地址")
    public Result<Address> getById(@PathVariable Long id) {
        Address address = addressService.getById(id);
        return Result.success(address);
    }

    /**
     * 根据id修改地址
     *
     * @param address
     * @return
     */
    @PutMapping
    @Operation(summary = "根据id修改地址")
    public Result update(@RequestBody Address address) {
        addressService.update(address);
        return Result.success();
    }

    /**
     * 设置默认地址
     *
     * @param address
     * @return
     */
    @PutMapping("/default")
    @Operation(summary = "设置默认地址")
    public Result setDefault(@RequestBody Address address) {
        addressService.setDefault(address);
        return Result.success();
    }

    /**
     * 根据id删除地址
     *
     * @param id
     * @return
     */
    @DeleteMapping
    @Operation(summary = "根据id删除地址")
    public Result deleteById(Long id) {
        addressService.deleteById(id);
        return Result.success();
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    @Operation(summary = "查询默认地址")
    public Result<Address> getDefault() {
        // 步骤1: 构建默认地址查询条件
        Address address = new Address();
        address.setIsDefault(1);
        address.setUserId(BaseContext.getCurrentId());
        // 步骤2: 查询地址列表
        List<Address> list = addressService.list(address);

        if (list != null && list.size() == 1) {
            return Result.success(list.get(0));
        }

        return Result.error("没有查询到默认地址");
    }

}