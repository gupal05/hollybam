package com.hollybam.hollybam.controller.productController;

import com.hollybam.hollybam.dto.CategoryDetailDto;
import com.hollybam.hollybam.dto.CategoryDto;
import com.hollybam.hollybam.services.CategoryService;
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

    @GetMapping("/list")
    public ModelAndView getProList(
            @RequestParam("categoryCode") String categoryCode,
            @RequestParam(value = "detailCode", required = false) String detailCode,
            ModelAndView mav) {

        CategoryDto cate = categoryService.selectCategoryWithDetails(categoryCode);

        Map<String, Object> active = new HashMap<>();
        if (detailCode != null) {
            active.put("detailCode", detailCode);
        }

        mav.addObject("active", active);
        mav.addObject("cate", cate);
        mav.setViewName("categories");
        return mav;
    }

}
