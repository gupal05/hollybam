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
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "12") int size,
            ModelAndView mav) {

        CategoryDto cate = categoryService.selectCategoryWithDetails(categoryCode);

        Map<String, Object> active = new HashMap<>();
        // detailCode가 null이어도 항상 키를 추가
        active.put("detailCode", detailCode);  // null일 수도 있지만 키는 존재

        // 페이징을 위한 offset 계산
        int offset = (page - 1) * size;

        // 상품 리스트 조회
        List<ProductDto> productList;
        int totalCount;

        if (detailCode != null && !detailCode.isEmpty()) {
            productList = productService.getProductsByCategoryAndDetailWithPaging(categoryCode, detailCode, sort, offset, size);
            totalCount = productService.getProductCountByCategoryAndDetail(categoryCode, detailCode);
        } else {
            productList = productService.getProductsByCategoryCodeWithPaging(categoryCode, sort, offset, size);
            totalCount = productService.getProductCountByCategory(categoryCode);
        }

        // 페이징 정보 계산
        int totalPages = (int) Math.ceil((double) totalCount / size);
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        mav.addObject("active", active);
        mav.addObject("cate", cate);
        mav.addObject("productList", productList);
        mav.addObject("currentSort", sort);

        // 페이지네이션 정보 추가
        mav.addObject("currentPage", page);
        mav.addObject("totalPages", totalPages);
        mav.addObject("totalCount", totalCount);
        mav.addObject("startPage", startPage);
        mav.addObject("endPage", endPage);
        mav.addObject("size", size);
        mav.setViewName("categories");
        return mav;
    }
}
