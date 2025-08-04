package com.hollybam.hollybam.controller.adminController;

import com.hollybam.hollybam.services.admin.AdminSpecialSaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/special/sale")
public class AdminSpecialSaleController {
    @Autowired
    private AdminSpecialSaleService adminSpecialSaleService;

    @GetMapping
    public ModelAndView createSpecialSalePage(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            ModelAndView mav) {

        // 카테고리 리스트 조회
        List<Map<String, Object>> cateList = adminSpecialSaleService.selectCategory();

        // 현재 날짜 정보
        LocalDate now = LocalDate.now();

        // 파라미터가 없으면 현재 날짜 사용
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        // 날짜 문자열 생성 (YYYY-MM-01 형식)
        String date = String.format("%d-%02d-01", targetYear, targetMonth);

        System.out.println("조회할 날짜: " + date);

        // 특가 상품 리스트 조회
        List<Map<String, Object>> saleList = adminSpecialSaleService.selectSpecialSaleList(date);

        // 모델에 데이터 추가
        mav.addObject("saleList", saleList);
        mav.addObject("cateList", cateList);
        mav.addObject("currentYear", targetYear);
        mav.addObject("currentMonth", targetMonth);
        mav.setViewName("admin/discount/createSpecialSale");

        return mav;
    }

    @GetMapping("/cate/detail")
    @ResponseBody
    public Map<String, Object> cateDetail(@RequestParam("categoryCode") String categoryCode) {
        List<Map<String, Object>> cdList = adminSpecialSaleService.selectCategoryDetail(categoryCode);
        return Map.of("cdList", cdList);
    }

    @GetMapping("/product")
    public ResponseEntity<?> productByCate(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String cateCode,
                                           @RequestParam(required = false) String cdCode) {
        List<Map<String, Object>> products = adminSpecialSaleService.searchProducts(cateCode, cdCode, keyword);

        // Map으로 감싸서 반환
        Map<String, Object> response = new HashMap<>();
        response.put("products", products);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addSpecialSaleProduct(@RequestBody List<Map<String, Object>> products) {
        for(int i=0;i<products.size();i++){
            int productCode = Integer.parseInt(products.get(i).get("productCode").toString());
            int originalPrice = adminSpecialSaleService.selectOriginalPrice(productCode);
            int salePrice = Integer.parseInt(products.get(i).get("salePrice").toString());
            int discountRate = 0;
            if (originalPrice > 0) {
                discountRate = (int) Math.round(((originalPrice - salePrice) / (double) originalPrice) * 100);
            }
            products.get(i).put("salePercent", discountRate);
            products.get(i).put("startDate", products.get(i).get("year")+"-"+products.get(i).get("month")+"-1");
        }

        if(adminSpecialSaleService.insertSpecialSaleProducts(products)>0){
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.badRequest().body("특가 상품 등록에 실패했습니다.");
        }
    }
}
