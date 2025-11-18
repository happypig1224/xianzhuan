package com.xianzhuan.mapper;

import com.xianzhuan.annotation.AutoFill;
import com.xianzhuan.entity.UserMessage;
import com.xianzhuan.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Mapper
public interface UserMessageMapper {

    /**
     * 新增消息
     */
    @AutoFill(OperationType.INSERT)
    void insert(UserMessage userMessage);

    /**
     * 根据id查询消息
     */
    @Select("select * from user_message where id = #{id}")
    UserMessage getById(Long id);

    /**
     * 根据用户id查询消息列表
     */
    List<UserMessage> listByUserId(Long userId);

    /**
     * 标记消息为已读
     */
    @Update("update user_message set is_read = 1 where id = #{id}")
    void markAsRead(Long id);

    /**
     * 统计未读消息数
     */
    @Select("select count(id) from user_message where user_id = #{userId} and is_read = 0")
    Integer countUnread(Long userId);
}