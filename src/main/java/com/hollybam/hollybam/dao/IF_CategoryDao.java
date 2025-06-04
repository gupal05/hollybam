package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.CategoryDto;
import java.util.List;

public interface IF_CategoryDao {
    List<CategoryDto> selectAllCategories();
}
