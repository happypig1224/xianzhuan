package com.xianzhuan.mapper;

import com.xianzhuan.annotation.AutoFill;
import com.xianzhuan.entity.ItemSpec;
import com.xianzhuan.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Mapper
public interface ItemSpecMapper {
    @AutoFill(OperationType.INSERT)
    void insertBatch(List<ItemSpec> specs);
    @Select("select * from item_spec where item_id=#{id}")
    List<ItemSpec> getByItemId(Long id);
    @Delete("delete from item_spec where item_id=#{id}")
    void deleteByItemId(Long id);
}