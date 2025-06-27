package com.hollybam.hollybam.controller.productController;

import com.hollybam.hollybam.dto.CategoryDetailDto;
import com.hollybam.hollybam.dto.CategoryDto;
import com.hollybam.hollybam.dto.ProductDto;
import com.hollybam.hollybam.services.CategoryService;
import com.hollybam.hollybam.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/product")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public ModelAndView getProList(
            @RequestParam("categoryCode") String categoryCode,
            @RequestParam(value = "detailCode", required = false) String detailCode,
            @RequestParam(value = "sort", required = false, defaultValue = "popular") String sort,
            ModelAndView mav) {

        CategoryDto cate = categoryService.selectCategoryWithDetails(categoryCode);

        Map<String, Object> active = new HashMap<>();
        // detailCode가 null이어도 항상 키를 추가
        active.put("detailCode", detailCode);  // null일 수도 있지만 키는 존재

        // 상품 리스트 조회
        List<ProductDto> productList;
        if (detailCode != null && !detailCode.isEmpty()) {
            productList = productService.getProductsByCategoryAndDetail(categoryCode, detailCode, sort);
        } else {
            productList = productService.getProductsByCategoryCode(categoryCode, sort);
        }

        mav.addObject("active", active);
        mav.addObject("cate", cate);
        mav.addObject("productList", productList);
        mav.addObject("currentSort", sort);
        mav.setViewName("categories");
        return mav;
    }
}
