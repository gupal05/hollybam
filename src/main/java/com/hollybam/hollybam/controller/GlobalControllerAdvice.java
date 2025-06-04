package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.CategoryDto;
import com.hollybam.hollybam.services.IF_CategoryService;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final IF_CategoryService categoryService;
    private List<CategoryDto> cachedCategories;

    public GlobalControllerAdvice(IF_CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 애플리케이션 시작 시 한번만 카테고리 리스트를 불러와 캐싱
    @PostConstruct
    public void init() {
        cachedCategories = categoryService.getCategoryList();
    }

    @ModelAttribute("categories")
    public List<CategoryDto> categories() {
        return cachedCategories;
    }

}

