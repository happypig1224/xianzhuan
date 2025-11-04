package com.xianzhuan.mapper;

import com.xianzhuan.entity.Address;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Mapper
public interface AddressMapper {

    /**
     * 条件查询
     */
    List<Address> list(Address address);

    /**
     * 新增
     */
    @Insert("insert into address" +
            "        (user_id, consignee, phone, sex, province_code, province_name, city_code, city_name, district_code," +
            "         district_name, detail, label, is_default)" +
            "        values (#{userId}, #{consignee}, #{phone}, #{sex}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}," +
            "                #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    void insert(Address address);

    /**
     * 根据id查询
     */
    @Select("select * from address where id = #{id}")
    Address getById(Long id);

    /**
     * 根据id修改
     */
    void update(Address address);

    /**
     * 根据用户id修改是否默认地址
     */
    @Update("update address set is_default = #{isDefault} where user_id = #{userId}")
    void updateIsDefaultByUserId(Address address);

    /**
     * 根据id删除地址
     */
    @Delete("delete from address where id = #{id}")
    void deleteById(Long id);

}