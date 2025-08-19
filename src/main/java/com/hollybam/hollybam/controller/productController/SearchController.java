package com.hollybam.hollybam.controller.productController;

import com.hollybam.hollybam.services.IF_CategoryService;
import com.hollybam.hollybam.services.IF_SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private IF_SearchService searchService;
    @Autowired
    private IF_CategoryService categoryService;

    @GetMapping
    public String search(
            Model model,
            @RequestParam String keyword,
            // page, size 파라미터를 optional 로 받되 기본값을 세팅
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="12") int size
    ) {
        // 1) 전체 개수
        int totalCount = searchService.countByKeyword(keyword);

        // 2) 몇 페이지까지 있는지 계산
        int totalPages = (totalCount + size - 1) / size;

        // 3) offset 계산
        int offset = (page - 1) * size;

        // 4) 실제 페이징된 데이터 조회
        Map<String,Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("offset", offset);
        params.put("size", size);
        List<Map<String,Object>> products = searchService.searchProductsByPage(params);
        System.out.println(products);
        for(int i=0;i<products.size();i++) {
            int productCode = (int) products.get(i).get("productCode");
            if(categoryService.isSpecialSale(productCode) > 0){
                products.get(i).remove("priceSelling");
                products.get(i).put("priceSelling", categoryService.getSpecialSalePrice(productCode));
                int selling = (int) products.get(i).get("priceSelling");
                int original = (int) products.get(i).get("priceOriginal");
                products.get(i).remove("discount_rate");
                double rate = ((double) (original - selling) /original)*100;
                products.get(i).put("discount_rate", Math.round(rate));
            }
        }

        // 5) 뷰로 넘길 속성들
        model.addAttribute("products", products);
        model.addAttribute("searchCount", totalCount);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "search";
    }
}
