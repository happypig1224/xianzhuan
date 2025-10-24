package com.xianzhuan.mapper;

import com.github.pagehelper.Page;
import com.xianzhuan.annotation.AutoFill;
import com.xianzhuan.dto.AdminPageQueryDTO;
import com.xianzhuan.entity.Admin;
import com.xianzhuan.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Mapper
public interface AdminMapper {

    /**
     * 根据用户名查询管理员
     */
    @Select("select * from admin where username = #{username}")
    Admin getByUsername(String username);
    @Insert("insert into admin(name,username,password,phone,sex,id_number,status,create_time,update_time,create_user,update_user)" +
    "values(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(OperationType.INSERT)
    void save(Admin admin);

    Page<Admin> pageQuery(AdminPageQueryDTO pageQueryDTO);
    @AutoFill(OperationType.UPDATE)
    void update(Admin admin);

    @Select("select * from admin where id=#{id}")
    Admin getById(Admin admin);
}