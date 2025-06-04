package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_CategoryDao;
import com.hollybam.hollybam.dto.CategoryDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService implements IF_CategoryService {

    private final IF_CategoryDao categoryDao;

    public CategoryService(IF_CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public List<CategoryDto> getCategoryList() {

        return categoryDao.selectAllCategories();
    }
}