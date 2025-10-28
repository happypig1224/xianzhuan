package com.xianzhuan.service;

import com.xianzhuan.dto.CategoryDTO;
import com.xianzhuan.dto.CategoryPageQueryDTO;
import com.xianzhuan.entity.Category;
import com.xianzhuan.result.PageResult;

import java.util.List;

public interface CategoryService {
    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    void save(CategoryDTO categoryDTO);

    CategoryDTO getById(Long id);

    void update(CategoryDTO categoryDTO);

    void deleteById(Long id);

    void startOrStop(Integer status, Long id);

    List<CategoryDTO> selectAll();

    List<Category> list(Integer type);
}
