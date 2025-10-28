package com.xianzhuan.mapper;

import com.github.pagehelper.Page;
import com.xianzhuan.annotation.AutoFill;
import com.xianzhuan.dto.CategoryDTO;
import com.xianzhuan.dto.CategoryPageQueryDTO;
import com.xianzhuan.entity.Category;
import com.xianzhuan.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    Page<Category> page(CategoryPageQueryDTO categoryPageQueryDTO);

    @AutoFill(OperationType.INSERT)
    void save(Category category);

    @Select("select * from category where id=#{id}")
    Category getById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    @Delete("delete from category where id=#{id}")
    void deleteById(Long id);

    @Select("select * from category")
    List<CategoryDTO> findAll();

    List<Category> list(Integer type);
}
