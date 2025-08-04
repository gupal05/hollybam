package com.hollybam.hollybam.controller.productController;

import com.hollybam.hollybam.dto.*;
import com.hollybam.hollybam.services.IF_ReviewService;
import com.hollybam.hollybam.services.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private IF_ReviewService reviewService;

    @GetMapping("/{productId}")
    public ModelAndView getProductPage(@PathVariable String productId, HttpSession session, ModelAndView mav) {
        // ===== 기존 코드 100% 유지 =====
        ProductDto productDto = new ProductDto();
        productDto.setProductId(productId);
        int productCode = productService.getProductCode(productDto);

        List<ImageDto> thumbnails = productService.getProductInfoThumbnail(productCode);
        List<ImageDto> contents = productService.getProductInfoContent(productCode);
        List<ProductOptionDto> options = productService.getProductOptions(productCode);

        // 옵션 이름들을 Set으로 추출
        Set<String> optionNames = options.stream()
                .map(ProductOptionDto::getOptionName)
                .collect(Collectors.toSet());

        productDto = productService.getProductDetailInfo_first(productId);
        productDto.setReviewAvg(productService.getProductReviewAvg(productDto.getProductCode()));
        productDto.setReviewCount(productService.getProductReviewCount(productDto.getProductCode()));
        System.out.println(productDto);

        // ===== 새로 추가: 리뷰 기능 =====
        try {
            // 현재 사용자 정보 확인
            Integer memCode = null;
            Integer guestCode = null;

            Object memberObj = session.getAttribute("member");
            Object guestObj = session.getAttribute("guest");

            if (memberObj != null) {
                MemberDto member = (MemberDto) memberObj;
                memCode = member.getMemberCode();
            } else if (guestObj != null) {
                GuestDto guest = (GuestDto) guestObj;
                guestCode = guest.getGuestCode();
            }

            // 초기 리뷰 데이터 조회 (페이지 로드 시 표시할 리뷰들)
            List<Map<String, Object>> initialPhotoReviews = reviewService.getProductPhotoReviews(
                    productCode, "latest", 1, 5, null, memCode, guestCode);

            List<Map<String, Object>> initialTextReviews = reviewService.getProductTextReviews(
                    productCode, "latest", 1, 5, null, memCode, guestCode);

            // 리뷰 카운트 정보
            Map<String, Object> reviewCount = reviewService.getProductReviewCount(productCode, null);
            Map<String, Object> reviewRatingCount = reviewService.getProductRatingCounts(productCode);

            // ⭐ 추가 방어 코드: reviewRatingCount가 null인 경우 기본값 설정
            if (reviewRatingCount == null) {
                reviewRatingCount = new HashMap<>();
                reviewRatingCount.put("cnt1", 0);
                reviewRatingCount.put("cnt2", 0);
                reviewRatingCount.put("cnt3", 0);
                reviewRatingCount.put("cnt4", 0);
                reviewRatingCount.put("cnt5", 0);
            }

            // 베스트 리뷰 (기존 메서드 활용)
            List<BestReviewDto> bestReviews = reviewService.selectBestReviewsByProduct(productCode);

            // 총 리뷰 수 계산
            // 평점별 카운트 꺼내기 (값이 null이면 0으로 처리)
            Number n1 = (Number) reviewRatingCount.getOrDefault("cnt1", 0);
            Number n2 = (Number) reviewRatingCount.getOrDefault("cnt2", 0);
            Number n3 = (Number) reviewRatingCount.getOrDefault("cnt3", 0);
            Number n4 = (Number) reviewRatingCount.getOrDefault("cnt4", 0);
            Number n5 = (Number) reviewRatingCount.getOrDefault("cnt5", 0);

            // ⭐ 추가 방어 코드: Number가 null인 경우 처리
            int cnt1 = (n1 != null) ? n1.intValue() : 0;
            int cnt2 = (n2 != null) ? n2.intValue() : 0;
            int cnt3 = (n3 != null) ? n3.intValue() : 0;
            int cnt4 = (n4 != null) ? n4.intValue() : 0;
            int cnt5 = (n5 != null) ? n5.intValue() : 0;

            // 총 리뷰 수 계산
            int totalReviews = cnt1 + cnt2 + cnt3 + cnt4 + cnt5;

            // 퍼센티지 계산
            double pct5 = totalReviews > 0 ? cnt5 * 100.0 / totalReviews : 0;
            double pct4 = totalReviews > 0 ? cnt4 * 100.0 / totalReviews : 0;
            double pct3 = totalReviews > 0 ? cnt3 * 100.0 / totalReviews : 0;
            double pct2 = totalReviews > 0 ? cnt2 * 100.0 / totalReviews : 0;
            double pct1 = totalReviews > 0 ? cnt1 * 100.0 / totalReviews : 0;

            // ModelAndView에 담기
            mav.addObject("rating5Percent", pct5);
            mav.addObject("rating4Percent", pct4);
            mav.addObject("rating3Percent", pct3);
            mav.addObject("rating2Percent", pct2);
            mav.addObject("rating1Percent", pct1);

            // 리뷰 관련 데이터 추가
            mav.addObject("reviewRatingCount", reviewRatingCount);
            mav.addObject("initialPhotoReviews", initialPhotoReviews);
            mav.addObject("initialTextReviews", initialTextReviews);
            mav.addObject("reviewCount", reviewCount);
            mav.addObject("bestReviews", bestReviews);
            mav.addObject("currentMemCode", memCode);
            mav.addObject("currentGuestCode", guestCode);

            if(productService.isSpecialSale(productDto.getProductCode()) > 0){
                productDto.setSale(true);
                productDto.setSalePrice(productService.getProductDetailSalePrice(productDto.getProductCode()));
                int originalPrice = productDto.getPriceDtoList().get(0).getPriceOriginal();
                int salePrice = productDto.getSalePrice();

                int discountRate = 0;
                if (originalPrice > 0) {
                    discountRate = (int) Math.round(((originalPrice - salePrice) / (double) originalPrice) * 100);
                }
                productDto.setSpecialDiscountRate(discountRate);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 리뷰 데이터 로드 실패 시 빈 데이터로 설정 (기존 기능은 유지)
            mav.addObject("initialPhotoReviews", new ArrayList<>());
            mav.addObject("initialTextReviews", new ArrayList<>());
            mav.addObject("reviewCount", Map.of("photoReviews", 0, "textReviews", 0));
            mav.addObject("bestReviews", new ArrayList<>());
            mav.addObject("currentMemCode", null);
            mav.addObject("currentGuestCode", null);
        }

        // ===== 기존 ModelAndView 설정 100% 유지 =====
        mav.addObject("thumbnails", thumbnails);
        mav.addObject("contents", contents);
        mav.addObject("product", productDto);
        mav.addObject("options", options);
        mav.addObject("optionNames", optionNames);
        mav.setViewName("product/productDetail");
        return mav;
    }

    /**
     * 상품별 리뷰 데이터 AJAX 조회 (탭 전환, 정렬, 필터링, 페이지네이션용)
     */
    @GetMapping("/{productId}/reviews")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(value = "type", defaultValue = "photo") String type,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "rating", required = false) Integer rating,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 상품 코드 조회
            ProductDto productDto = new ProductDto();
            productDto.setProductId(productId);
            int productCode = productService.getProductCode(productDto);

            // 현재 사용자 정보 조회
            Integer memCode = null;
            Integer guestCode = null;

            Object memberObj = session.getAttribute("member");
            Object guestObj = session.getAttribute("guest");

            if (memberObj != null) {
                MemberDto member = (MemberDto) memberObj;
                memCode = member.getMemberCode();
            } else if (guestObj != null) {
                GuestDto guest = (GuestDto) guestObj;
                guestCode = guest.getGuestCode();
            }

            List<Map<String, Object>> reviewList = new ArrayList<>();

            // 리뷰 타입에 따라 데이터 조회
            if ("photo".equals(type)) {
                reviewList = reviewService.getProductPhotoReviews(productCode, sort, page, size, rating, memCode, guestCode);
            } else if ("text".equals(type)) {
                reviewList = reviewService.getProductTextReviews(productCode, sort, page, size, rating, memCode, guestCode);
            }

            // 리뷰 카운트 정보 (필터링 적용)
            Map<String, Object> reviewCount = reviewService.getProductReviewCount(productCode, rating);

            response.put("success", true);
            response.put("reviewList", reviewList);
            response.put("photoReviews", reviewCount.get("photoReviews"));
            response.put("textReviews", reviewCount.get("textReviews"));
            response.put("currentType", type);
            response.put("currentSort", sort);
            response.put("currentPage", page);
            response.put("currentRating", rating);
            response.put("hasMore", reviewList.size() == size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "리뷰 데이터를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
