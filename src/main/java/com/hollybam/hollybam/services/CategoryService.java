package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_CategoryDao;
import com.hollybam.hollybam.dto.CategoryDetailDto;
import com.hollybam.hollybam.dto.CategoryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService implements IF_CategoryService {

    private final IF_CategoryDao categoryDao;

    public CategoryService(IF_CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    @Transactional
    public List<CategoryDto> getCategoryList() {

        return categoryDao.selectAllCategories();
    }

    @Override
    @Transactional
    public List<CategoryDetailDto> selectAllCategoriesDetails(String cateCode) {
        return categoryDao.selectAllCategoriesDetails(cateCode);
    }

    @Override
    @Transactional
    public int getCateDetailCount(String cateCode) {
        return categoryDao.getCateDetailCount(cateCode);
    }

    @Override
    @Transactional
    public CategoryDto selectCategoryWithDetails(String cateCode){
        return categoryDao.selectCategoryWithDetails(cateCode);
    }

}