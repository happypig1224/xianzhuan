package com.xianzhuan.mapper;

import com.xianzhuan.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);

    /**
     * 插入用户
     * @param user
     */
    void insert(User user);

    @Select("select * from user where id=#{userId}")
    User getById(Long userId);

    Integer countByMap(Map map);
}
