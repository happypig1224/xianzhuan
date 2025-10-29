package com.xianzhuan.mapper;

import com.github.pagehelper.Page;
import com.xianzhuan.annotation.AutoFill;
import com.xianzhuan.dto.UsedItemPageQueryDTO;
import com.xianzhuan.entity.UsedItem;
import com.xianzhuan.enumeration.OperationType;
import com.xianzhuan.vo.UsedItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Mapper
public interface ItemMapper {

    /**
     * 根据分类id查询商品数量
     */
    @Select("select count(id) from used_item where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
    void insert(UsedItem usedItem);

    Page<UsedItemVO> queryPage(UsedItemPageQueryDTO usedItemPageQueryDTO);

    @Delete("delete from used_item where id=#{itemId}")
    void deleteByIds(Long itemId);

    @Select("select * from used_item where id=#{id}")
    UsedItem getById(Long id);

    @AutoFill(OperationType.UPDATE)
    void updateItem(UsedItem usedItem);

    List<UsedItem> list(UsedItem usedItem);

    // BUG-1修复: countByMap原引用setmeal表，改为used_item
    Integer countByMap(Map map);

    // 获取前100条商品，按销量降序排序
    @Select("select * from used_item order by sales_count desc limit 100")
    List<UsedItem> getAllTop100();

    @Select("select name from used_item order by sales_count desc limit 500")
    List<String> getTop500ItemNames();

    @Select("select id from used_item where name=#{itemName}")
    Long getByName(String itemName);

    @Select("select name from used_item order by sales_count desc limit 100")
    List<String> getAllTop100Name();
}