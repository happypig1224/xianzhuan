package com.xianzhuan.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xianzhuan.constant.StatusConstant;
import com.xianzhuan.dto.CategoryDTO;
import com.xianzhuan.dto.CategoryPageQueryDTO;
import com.xianzhuan.entity.Category;
import com.xianzhuan.mapper.CategoryMapper;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO
                .getPageSize());
        Page<Category> page=categoryMapper.page(categoryPageQueryDTO);
        return new  PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category=new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setStatus(StatusConstant.ENABLE);
        categoryMapper.save(category);
    }

    @Override
    public CategoryDTO getById(Long id) {
        Category category= categoryMapper.getById(id);
        CategoryDTO categoryDTO=new CategoryDTO();
        BeanUtils.copyProperties(category,categoryDTO);
        return categoryDTO;
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category=new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.update(category);
    }

    @Override
    public void deleteById(Long id) {
        categoryMapper.deleteById(id);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Category category= Category.builder()
                .status(status).id(id).build();
        categoryMapper.update(category);
    }

    @Override
    public List<CategoryDTO> selectAll() {
        return categoryMapper.findAll();
    }

    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }


}
