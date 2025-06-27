package com.hollybam.hollybam.controller.adminController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hollybam.hollybam.dto.*;
import com.hollybam.hollybam.services.CouponService;
import com.hollybam.hollybam.services.DiscountService;
import com.hollybam.hollybam.services.ProductService;
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
    private HttpSession session;

    @GetMapping
    public String admin(Model model) {
        if(session.getAttribute("member") != null) {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if(member.getMemberRole().equals("admin")) {
                return  "admin/dashboard";
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
                model.addAttribute("member", member);
                return "admin/dashboard";
            }
        } else  {
            return "redirect:/";
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

        String uploadDir = "src/main/resources/static/testImage/";

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
        if (file == null || file.isEmpty()) {
            return; // 비어있으면 저장하지 않음
        }

        Path path = Paths.get(uploadDir + file.getOriginalFilename());

        // 디렉토리 없으면 생성
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // 파일 저장
        file.transferTo(path);
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
            // 옵션이 있는 경우: 모든 옵션의 재고 합계를 전체 재고로 설정
            int totalQuantity = productOptions.stream()
                    .mapToInt(ProductOptionDto::getOptionQuantity)
                    .sum();
            productDto.setProductQuantity(totalQuantity);
        }

        productDtoList.add(productDto);
        categoryDetailDto.setProductList(productDtoList);
        categoryDetailDtoList.add(categoryDetailDto);
        categoryDto.setCategoryDetailDto(categoryDetailDtoList);

        System.out.println(categoryDto);

        if(productService.insertProduct(categoryDto) > 0){
            categoryDto.getCategoryDetailDto().get(0).getProductList().get(0).setProductCode(productService.getProductCode(productDto));
            ProductDto pro = categoryDto.getCategoryDetailDto().get(0).getProductList().get(0);

            if(productService.insertPrice(pro) > 0){
                if(productService.insertImage(pro) > 0){
                    // 옵션이 있는 경우 옵션도 저장
                    if (!productOptions.isEmpty()) {
                        if(productService.insertProductOptions(pro) > 0) { // 옵션 저장 메소드
                            message = "상품이 등록 되었습니다.";
                        } else {
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
        model.addAttribute("coupon", new CouponDto());
        model.addAttribute("discount", new DiscountDto()); // 할인코드 폼에 바인딩
        return "admin/discount/discountCreate"; // ← 여기서 쿠폰/할인코드 둘 다 있는 폼
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
