package com.hollybam.hollybam.controller.adminController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hollybam.hollybam.dto.*;
import com.hollybam.hollybam.services.CouponService;
import com.hollybam.hollybam.services.DiscountService;
import com.hollybam.hollybam.services.ProductService;
import com.hollybam.hollybam.services.admin.AdminDashboardServiceImpl;
import com.hollybam.hollybam.util.S3Uploader;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private AdminDashboardServiceImpl adminDashboardService;
    @Autowired
    private HttpSession session;
    @Autowired
    private S3Uploader s3Uploader;

    @GetMapping
    public String admin(Model model) {
        if(session.getAttribute("member") != null) {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if(member.getMemberRole().equals("admin")) {
                return  "redirect:/admin/dashboard";
            } else {
                return "redirect:/";
            }
        } else {
            return "admin/main";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        if(session.getAttribute("member") != null) {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if(!member.getMemberRole().equals("admin")) {
                return "redirect:/";
            } else  {
                // 총 주문 수량
                model.addAttribute("orderCount", adminDashboardService.adminSelectOrderCount());
                // 회원 수(비회원 제외)
                model.addAttribute("memberCount", adminDashboardService.adminSelectMemberCount());
                // 총 상품 수량
                model.addAttribute("productCount", adminDashboardService.adminSelectProductCount());
                // 총 결제 금액(취소/환불 고려 X)
                model.addAttribute("totalPaymentAmount", adminDashboardService.adminSelectTotalPaymentAmount(null, null));
                // 취소/환불 금액
                model.addAttribute("cancelAmount", adminDashboardService.adminSelectCancelAmount(null, null));
                // 총 결제 금액 - 취소/환불 금액
//                model.addAttribute("salesAmount", adminDashboardService.adminSelectSalesAmount());
                // 순수익(할인 금액을 제외한)
                model.addAttribute("margin",  adminDashboardService.adminSelectMargin(null, null));

                model.addAttribute("payPendingCount", adminDashboardService.getPaymentStatusCount("PENDING"));

                System.out.println(adminDashboardService.getDescOrder());
                model.addAttribute("orderList", adminDashboardService.getDescOrder());

                model.addAttribute("paidCount", adminDashboardService.getPaymentStatusCount("PAID"));
                model.addAttribute("pendingCount", adminDashboardService.getOrderStatusCount("PENDING"));
                model.addAttribute("shippingCount", adminDashboardService.getDeliveryStatusCount("SHIPPING"));
                model.addAttribute("deliveredCount", adminDashboardService.getDeliveryStatusCount("DELIVERED"));
                model.addAttribute("returnRequestedCount", adminDashboardService.getPaymentStatusCount("RETURN_REQUESTED"));
                model.addAttribute("returningCount",  adminDashboardService.getDeliveryStatusCount("RETURNING"));
                model.addAttribute("refundPending",  adminDashboardService.getPaymentStatusCount("REFUND_PENDING"));
                model.addAttribute("refundedCount", adminDashboardService.getPaymentStatusCount("REFUNDED"));
                model.addAttribute("cancelCount",  adminDashboardService.getPaymentStatusCount("CANCELLED"));


                model.addAttribute("member", member);
                return "admin/dashboard";
            }
        } else  {
            return "redirect:/";
        }
    }

    @GetMapping("/sales/stat")
    @ResponseBody
    public Map<String, Object> updateSalesStat(@RequestParam(value = "startDate", required = false)LocalDate startDate,
                                               @RequestParam(value = "endDate", required = false)LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        // 총 결제 금액(취소/환불 고려 X)
        result.put("totalPaymentAmount", adminDashboardService.adminSelectTotalPaymentAmount(startDate, endDate));
        // 취소/환불 금액
        result.put("cancelAmount", adminDashboardService.adminSelectCancelAmount(startDate, endDate));
        // 순수익(할인 금액을 제외한)
        result.put("margin",  adminDashboardService.adminSelectMargin(startDate, endDate));
        return result;
    }

    /**
     * 매출 차트 데이터 조회 API
     * 기간에 따라 자동으로 일별/월별/연도별로 조회 단위 결정
     *
     * @param startDate 시작일 (필수)
     * @param endDate 종료일 (필수)
     * @return 매출 차트 데이터
     */
    @GetMapping("/sales/chart")
    @ResponseBody
    public Map<String, Object> getSalesChart(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        try {
            log.info("매출 차트 조회 요청: {} ~ {}", startDate, endDate);

            // 날짜 유효성 검증
            if (startDate.isAfter(endDate)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "시작일이 종료일보다 늦을 수 없습니다.");
                return errorResponse;
            }

            // 차트 데이터 조회
            Map<String, Object> result = adminDashboardService.getSalesChartData(startDate, endDate);

            log.info("매출 차트 조회 완료: {} 타입", result.get("periodType"));
            return result;

        } catch (Exception e) {
            log.error("매출 차트 조회 중 오류 발생", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "매출 차트 데이터를 불러오는 중 오류가 발생했습니다.");
            return errorResponse;
        }
    }

    @GetMapping("/add/product")
    public String addProduct(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "admin/addProduct";
    }

    // 이미지 업로드 처리
    @PostMapping("/upload/images")
    @ResponseBody
    public Map<String, Boolean> uploadImages(
            @RequestParam("titleImage") MultipartFile titleImage,  // ✅ 필수
            @RequestParam(value = "titleImage2", required = false) MultipartFile titleImage2, // ❌ 선택
            @RequestParam(value = "thumbnails[]", required = false) List<MultipartFile> thumbnails, // ❌ 선택
            @RequestParam("detailImages[]") List<MultipartFile> detailImages // ✅ 필수
    ) throws IOException {

        Map<String, Boolean> response = new HashMap<>();
        boolean uploadStatus = true;

        String uploadDir = "product";

        try {
            // 필수 타이틀 이미지 저장
            saveFile(titleImage, uploadDir);

            // 선택 타이틀 이미지2 저장
            if (titleImage2 != null && !titleImage2.isEmpty()) {
                saveFile(titleImage2, uploadDir);
            }

            // 선택 썸네일 이미지 저장
            if (thumbnails != null) {
                for (MultipartFile thumbnail : thumbnails) {
                    if (thumbnail != null && !thumbnail.isEmpty()) {
                        saveFile(thumbnail, uploadDir);
                    }
                }
            }

            // 필수 상세 이미지 저장
            for (MultipartFile detailImage : detailImages) {
                if (detailImage != null && !detailImage.isEmpty()) {
                    saveFile(detailImage, uploadDir);
                }
            }

        } catch (IOException e) {
            System.out.println("파일 업로드 실패: " + e.getMessage());
            uploadStatus = false;
        }

        response.put("status", uploadStatus);
        return response;
    }

    // 파일 저장 메서드
    private void saveFile(MultipartFile file, String uploadDir) throws IOException {
        if (file == null || file.isEmpty()) return;

        s3Uploader.upload(file, uploadDir);  // S3에 업로드
    }


    @PostMapping("/upload/is-product-code")
    @ResponseBody
    public Boolean isProductCode(@RequestParam("productCode") String code) {
        return productService.isProductCode(code) > 0;
    }

    @PostMapping("/upload/product")
    @ResponseBody
    public Map<String, Object> products(@RequestParam("productDto") String productDtoJson,
                                        @RequestParam("price") String price,
                                        @RequestParam("category") String category,
                                        @RequestParam("cateDetailCode") String cateDetailCode,
                                        @RequestParam(value = "productOptions", required = false) String productOptionsJson,
                                        @RequestPart("titleImage") MultipartFile titleImage,
                                        @RequestPart(name = "titleImage2", required = false) MultipartFile titleImage2,
                                        @RequestPart(name = "thumbnails[]", required = false) List<MultipartFile> thumbnails,
                                        @RequestPart("detailImages[]") List<MultipartFile> detailImages) throws IOException {
        Map<String, Object> response = new HashMap<>();
        Boolean status = true;
        String message = null;
        ObjectMapper objectMapper = new ObjectMapper();
        List<ImageDto> imgList = new ArrayList<>();
        List<PriceDto> priceList = new ArrayList<>();
        List<ProductDto> productDtoList = new ArrayList<>();
        List<CategoryDetailDto> categoryDetailDtoList = new ArrayList<>();

        // 기존 객체 생성 코드들...
        ImageDto imageDto = new ImageDto();
        ProductDto productDto = objectMapper.readValue(productDtoJson, ProductDto.class);
        PriceDto priceDto = objectMapper.readValue(price, PriceDto.class);
        CategoryDto categoryDto = objectMapper.readValue(category, CategoryDto.class);
        CategoryDetailDto categoryDetailDto = objectMapper.readValue(cateDetailCode, CategoryDetailDto.class);

        // 옵션 데이터 파싱 (옵션이 있는 경우에만)
        List<ProductOptionDto> productOptions = new ArrayList<>();
        if (productOptionsJson != null && !productOptionsJson.trim().isEmpty()) {
            // JSON 배열을 List<Map>으로 파싱
            List<Map<String, Object>> optionsData = objectMapper.readValue(productOptionsJson,
                    new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> optionData : optionsData) {
                String optionName = (String) optionData.get("optionName");
                List<Map<String, Object>> values = (List<Map<String, Object>>) optionData.get("values");

                for (Map<String, Object> valueData : values) {
                    ProductOptionDto optionDto = new ProductOptionDto();
                    optionDto.setOptionName(optionName);
                    optionDto.setOptionValue((String) valueData.get("optionValue"));
                    optionDto.setOptionPrice((Integer) valueData.get("optionPrice"));
                    optionDto.setOptionQuantity((Integer) valueData.get("optionQuantity"));
                    optionDto.setOptionCost((Integer) valueData.get("optionCost"));
                    productOptions.add(optionDto);
                }
            }
        }

        // 기존 이미지 처리 코드들...
        imageDto.setImageUrl(titleImage.getOriginalFilename());
        imageDto.setImageType("title");
        imageDto.setImageOrder(0);
        imgList.add(imageDto);

        imageDto = new ImageDto();
        imageDto.setImageUrl(titleImage2.getOriginalFilename());
        imageDto.setImageType("webp");
        imageDto.setImageOrder(0);
        imgList.add(imageDto);

        for(int i=0; i<thumbnails.size(); i++){
            imageDto = new ImageDto();
            imageDto.setImageUrl(thumbnails.get(i).getOriginalFilename());
            imageDto.setImageType("thumbnail");
            imageDto.setImageOrder(i);
            imgList.add(imageDto);
        }
        for(int i=0; i<detailImages.size(); i++){
            imageDto = new ImageDto();
            imageDto.setImageUrl(detailImages.get(i).getOriginalFilename());
            imageDto.setImageType("content");
            imageDto.setImageOrder(i);
            imgList.add(imageDto);
        }

        // 기존 데이터 설정
        productDto.setImageDtoList(imgList);
        priceDto.setPriceMargin(priceDto.getPriceSelling()-priceDto.getPriceCost());
        priceList.add(priceDto);
        productDto.setPriceDtoList(priceList);

        // 옵션 데이터를 ProductDto에 설정
        productDto.setProductOptionDtoList(productOptions); // ProductDto에 옵션 리스트 필드가 있다고 가정

        // 전체 재고 계산 로직 추가
        if (!productOptions.isEmpty()) {
            priceDto.setPriceCost(0);
            priceDto.setPriceMargin(0);
            for (ProductOptionDto option : productOptions) {
                OptionPriceDto optionPriceDto = new OptionPriceDto();
                // 마진 = (기본 판매가 + 옵션 추가가) - 옵션 원가
                int margin = (priceDto.getPriceSelling() + option.getOptionPrice()) - option.getOptionCost();
                optionPriceDto.setOptionPriceMargin(margin);
                option.setOptionPriceDto(optionPriceDto);
            }
            // 옵션이 있는 경우: 모든 옵션의 재고 합계를 전체 재고로 설정
            int totalQuantity = productOptions.stream()
                    .mapToInt(ProductOptionDto::getOptionQuantity)
                    .sum();
            productDto.setProductQuantity(totalQuantity);
        } else {
            // 옵션이 없는 경우: 기존 로직 유지
            priceDto.setPriceMargin(priceDto.getPriceSelling() - priceDto.getPriceCost());
        }

        productDtoList.add(productDto);
        categoryDetailDto.setProductList(productDtoList);
        categoryDetailDtoList.add(categoryDetailDto);
        categoryDto.setCategoryDetailDto(categoryDetailDtoList);


        if(productService.insertProduct(categoryDto) > 0){
            categoryDto.getCategoryDetailDto().get(0).getProductList().get(0).setProductCode(productService.getProductCode(productDto));
            ProductDto pro = categoryDto.getCategoryDetailDto().get(0).getProductList().get(0);

            if(productService.insertPrice(pro) > 0){
                if(productService.insertImage(pro) > 0){
                    // 옵션이 있는 경우 옵션도 저장
                    if (!productOptions.isEmpty()) {
                        try {
                            productService.saveOptionsAndPrices(pro);
                            message = "상품이 등록 되었습니다.";
                        } catch (Exception e) {
                            status = false;
                            message = "상품 옵션 등록이 실패 되었습니다.";
                        }
                    } else {
                        message = "상품이 등록 되었습니다.";
                    }
                } else {
                    status = false;
                    message = "상품 등록이 실패 되었습니다. 잠시 후에 다시 시도해주세요.";
                }
            } else {
                status = false;
                message = "상품 등록이 실패 되었습니다. 잠시 후에 다시 시도해주세요.";
            }
        } else {
            status = false;
            message = "상품 등록이 실패 되었습니다. 잠시 후에 다시 시도해주세요.";
        }

        response.put("status", status);
        response.put("message", message);
        return response;
    }

    // 쿠폰/할인코드 생성 페이지 진입
    @GetMapping("/discount/create")
    public String showCreateForm(Model model) {
        model.addAttribute("discount", new DiscountDto()); // 할인코드 폼에 바인딩
        return "admin/discount/discountCreate"; // ← 여기서 쿠폰/할인코드 둘 다 있는 폼
    }

    @GetMapping("/coupon/create")
    public String moveCreateCoupon(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status
    ) {
        model.addAttribute("coupon", new CouponDto());

        // offset 계산
        int offset = page * size;

        // 쿠폰 리스트 조회
        List<Map<String, Object>> couponList = couponService.selectCouponList(search, status, size, offset);

        // 총 개수 조회
        int totalCount = couponService.adminSelectCouponCount(search, status);

        // 통계 조회
        Map<String, Object> stats = couponService.selectCouponStats();

        // 페이지네이션 계산
        int totalPages = (int) Math.ceil((double) totalCount / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        // 모델에 데이터 추가
        model.addAttribute("coupons", couponList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalCount);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrevious", hasPrevious);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentStatus", status);

        return "admin/discount/createCoupon";
    }

    // 쿠폰 생성 처리
    @PostMapping("/coupon/create")
    public String createCoupon(@ModelAttribute CouponDto couponDto) {
        couponService.insertCoupon(couponDto);
        return "redirect:/admin/discount/create?success";
    }

    // 할인코드 생성 처리
    @PostMapping("/discount/create")
    public String createDiscount(@ModelAttribute DiscountDto discountDto) {
        discountService.insertDiscount(discountDto);
        return "redirect:/admin/discount/create?discountSuccess";
    }
}
