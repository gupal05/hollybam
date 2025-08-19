package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.CategoryDetailDto;
import com.hollybam.hollybam.dto.CategoryDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IF_CategoryService {
    List<CategoryDto> getCategoryList();
    int getCateDetailCount(String cateCode);
    List<CategoryDetailDto> selectAllCategoriesDetails(String cateCode);
    CategoryDto selectCategoryWithDetails(String cateCode);
    int isSpecialSale(@Param("productCode") int productCode);
    int getSpecialSalePrice(@Param("productCode") int productCode);
}
