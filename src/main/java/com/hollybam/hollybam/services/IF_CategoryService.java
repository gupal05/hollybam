package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.CategoryDetailDto;
import com.hollybam.hollybam.dto.CategoryDto;
import java.util.List;

public interface IF_CategoryService {
    List<CategoryDto> getCategoryList();
    int getCateDetailCount(String cateCode);
    List<CategoryDetailDto> selectAllCategoriesDetails(String cateCode);
    CategoryDto selectCategoryWithDetails(String cateCode);
}
