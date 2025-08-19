package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.CategoryDetailDto;
import com.hollybam.hollybam.dto.CategoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IF_CategoryDao {
    List<CategoryDto> selectAllCategories();
    int getCateDetailCount(String cateCode);
    List<CategoryDetailDto> selectAllCategoriesDetails(String cateCode);
    CategoryDto selectCategoryWithDetails(String cateCode);
    int isSpecialSale(@Param("productCode") int productCode);
    int getSpecialSalePrice(@Param("productCode") int productCode);
}
