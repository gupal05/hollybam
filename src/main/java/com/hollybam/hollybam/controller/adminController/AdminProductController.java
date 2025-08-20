package com.hollybam.hollybam.controller.adminController;

import com.hollybam.hollybam.dto.CategoryDto;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.ProductDto;
import com.hollybam.hollybam.services.admin.IF_AdminProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final IF_AdminProductService adminProductService;

    /**
     * 관리자 상품 목록 페이지
     */
    @GetMapping("/list")
    public String adminProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            Model model, HttpSession session) {
        if(session.getAttribute("member") != null){
            MemberDto mem = (MemberDto) session.getAttribute("member");
            if(!mem.getMemberRole().equals("admin")){
                return "redirect:/";
            } else {
                try {
                    // 검색 조건 설정
                    AdminProductSearchDto searchDto = AdminProductSearchDto.builder()
                            .page(page)
                            .size(size)
                            .categoryCode(categoryCode)
                            .searchType(searchType)
                            .searchKeyword(searchKeyword)
                            .status(status)
                            .sortBy(sortBy)
                            .sortOrder(sortOrder)
                            .build();

                    // 상품 목록 조회
                    AdminProductListResultDto result = adminProductService.adminGetProductList(searchDto);

                    // 카테고리 목록 조회 (필터용)
                    List<CategoryDto> categories = adminProductService.adminGetAllCategories();

                    // 페이징 정보 계산
                    int totalPages = (int) Math.ceil((double) result.getTotalCount() / size);
                    int startPage = Math.max(1, page - 2);
                    int endPage = Math.min(totalPages, page + 2);

                    model.addAttribute("products", result.getProducts());
                    model.addAttribute("categories", categories);
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", totalPages);
                    model.addAttribute("totalCount", result.getTotalCount());
                    model.addAttribute("startPage", startPage);
                    model.addAttribute("endPage", endPage);
                    model.addAttribute("size", size);
                    model.addAttribute("searchDto", searchDto);

                    log.info("관리자 상품 목록 조회 - 페이지: {}, 총 개수: {}", page, result.getTotalCount());

                } catch (Exception e) {
                    log.error("관리자 상품 목록 조회 중 오류 발생", e);
                    model.addAttribute("errorMessage", "상품 목록을 불러오는 중 오류가 발생했습니다.");
                }

                return "admin/product/list";
            }
        } else  {
            return "redirect:/admin";
        }

    }

    /**
     * 상품 활성화/비활성화 토글
     */
    @GetMapping("/toggle-status")
    public String adminToggleProductStatus(@RequestParam int productCode) {
        try {
            adminProductService.adminToggleProductStatus(productCode);
            log.info("상품 상태 변경 - 상품코드: {}", productCode);
        } catch (Exception e) {
            log.error("상품 상태 변경 중 오류 발생 - 상품코드: {}", productCode, e);
        }

        return "redirect:/admin/product/list";
    }

    /**
     * 상품 삭제 (소프트 삭제)
     */
    @GetMapping("/delete")
    public String adminDeleteProduct(@RequestParam int productCode) {
        try {
            adminProductService.adminDeleteProduct(productCode);
            log.info("상품 삭제 - 상품코드: {}", productCode);
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생 - 상품코드: {}", productCode, e);
        }

        return "redirect:/admin/product/list";
    }

    // 검색 조건을 담을 DTO 클래스
    public static class AdminProductSearchDto {
        private int page;
        private int size;
        private String categoryCode;
        private String searchType;
        private String searchKeyword;
        private String status;
        private String sortBy;
        private String sortOrder;

        public static AdminProductSearchDtoBuilder builder() {
            return new AdminProductSearchDtoBuilder();
        }

        // Getters and Setters
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public String getCategoryCode() { return categoryCode; }
        public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
        public String getSearchType() { return searchType; }
        public void setSearchType(String searchType) { this.searchType = searchType; }
        public String getSearchKeyword() { return searchKeyword; }
        public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

        public static class AdminProductSearchDtoBuilder {
            private AdminProductSearchDto dto = new AdminProductSearchDto();

            public AdminProductSearchDtoBuilder page(int page) {
                dto.setPage(page);
                return this;
            }

            public AdminProductSearchDtoBuilder size(int size) {
                dto.setSize(size);
                return this;
            }

            public AdminProductSearchDtoBuilder categoryCode(String categoryCode) {
                dto.setCategoryCode(categoryCode);
                return this;
            }

            public AdminProductSearchDtoBuilder searchType(String searchType) {
                dto.setSearchType(searchType);
                return this;
            }

            public AdminProductSearchDtoBuilder searchKeyword(String searchKeyword) {
                dto.setSearchKeyword(searchKeyword);
                return this;
            }

            public AdminProductSearchDtoBuilder status(String status) {
                dto.setStatus(status);
                return this;
            }

            public AdminProductSearchDtoBuilder sortBy(String sortBy) {
                dto.setSortBy(sortBy);
                return this;
            }

            public AdminProductSearchDtoBuilder sortOrder(String sortOrder) {
                dto.setSortOrder(sortOrder);
                return this;
            }

            public AdminProductSearchDto build() {
                return dto;
            }
        }
    }

    // 검색 결과를 담을 DTO 클래스
    public static class AdminProductListResultDto {
        private List<ProductDto> products;
        private long totalCount;

        public AdminProductListResultDto(List<ProductDto> products, long totalCount) {
            this.products = products;
            this.totalCount = totalCount;
        }

        public List<ProductDto> getProducts() { return products; }
        public void setProducts(List<ProductDto> products) { this.products = products; }
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    }

    @GetMapping("/export/excel")
    public void adminExportProductExcel(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            HttpServletResponse response) {

        try {
            // 검색 조건 설정 (페이징 없이 전체 데이터)
            AdminProductSearchDto searchDto = AdminProductSearchDto.builder()
                    .page(1)
                    .size(Integer.MAX_VALUE) // 전체 데이터
                    .categoryCode(categoryCode)
                    .searchType(searchType)
                    .searchKeyword(searchKeyword)
                    .status(status)
                    .sortBy(sortBy)
                    .sortOrder(sortOrder)
                    .build();

            // 상품 목록 조회
            AdminProductListResultDto result = adminProductService.adminGetProductList(searchDto);

            // Excel 파일 생성
            adminProductService.adminCreateProductExcel(result.getProducts(), response);

            log.info("관리자 상품 목록 Excel 내보내기 완료 - 상품 수: {}", result.getProducts().size());

        } catch (Exception e) {
            log.error("Excel 내보내기 중 오류 발생", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Excel 파일 생성 중 오류가 발생했습니다.");
            } catch (IOException ioException) {
                log.error("응답 전송 중 오류 발생", ioException);
            }
        }
    }

    /**
     * 상품 일괄 상태 변경
     */
    @PostMapping("/bulk-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminBulkStatusChange(@RequestBody AdminBulkStatusDto bulkStatusDto) {
        Map<String, Object> result = new HashMap<>();

        try {
            adminProductService.adminBulkUpdateProductStatus(bulkStatusDto.getProductCodes(), bulkStatusDto.getStatus());

            result.put("success", true);
            result.put("message", "상품 상태가 성공적으로 변경되었습니다.");

            log.info("관리자 상품 일괄 상태 변경 완료 - 상품 수: {}, 상태: {}",
                    bulkStatusDto.getProductCodes().size(), bulkStatusDto.getStatus());

        } catch (Exception e) {
            log.error("상품 일괄 상태 변경 중 오류 발생", e);
            result.put("success", false);
            result.put("message", "상품 상태 변경 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 상품 상세 정보 조회 (AJAX)
     */
    @GetMapping("/detail/{productCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminGetProductDetailAjax(@PathVariable int productCode) {
        Map<String, Object> result = new HashMap<>();

        try {
            ProductDto product = adminProductService.adminGetProductDetail(productCode);

            result.put("success", true);
            result.put("product", product);

            log.info("관리자 상품 상세 정보 AJAX 조회 완료 - 상품코드: {}", productCode);

        } catch (Exception e) {
            log.error("상품 상세 정보 AJAX 조회 중 오류 발생 - 상품코드: {}", productCode, e);
            result.put("success", false);
            result.put("message", "상품 정보를 불러오는 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    // 일괄 상태 변경을 위한 DTO 클래스
    public static class AdminBulkStatusDto {
        private List<Integer> productCodes;
        private int status;

        // Getters and Setters
        public List<Integer> getProductCodes() { return productCodes; }
        public void setProductCodes(List<Integer> productCodes) { this.productCodes = productCodes; }
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
    }

    @GetMapping("/edit")
    public String adminEditProduct(@RequestParam  int productCode, Model model) {
        model.addAttribute("product", adminProductService.getProductInfo(productCode));
        model.addAttribute("thumbnails", adminProductService.getEditProductThumbnail(productCode));
        model.addAttribute("details", adminProductService.getEditProductDetail(productCode));
        model.addAttribute("optionName",  adminProductService.getOptionName(productCode));
        model.addAttribute("options",  adminProductService.getProductOption(productCode));
        System.out.println(adminProductService.getProductOption(productCode));
        return "admin/product/editProduct";
    }

    @GetMapping("/pick")
    public String adminHollybamPick(Model model){
        List<Map<String, Object>> pickList = adminProductService.getPickList();
        model.addAttribute("count", adminProductService.getPickCount());
        model.addAttribute("pickList", pickList);
        model.addAttribute("productList", adminProductService.getProductList());
        return "admin/product/hollybamPick";
    }

    @PostMapping("/pick/search")
    @ResponseBody
    public List<Map<String, Object>> adminHollybamPickSearch(@RequestParam String keyword, @RequestParam String categoryCode, @RequestParam String cateDetailCode) {
        return adminProductService.searchForPickProducts(keyword, categoryCode, cateDetailCode);
    }

    @PostMapping("/pick/add")
    @ResponseBody
    public Map<String, Object> addHollybamPick(@RequestBody List<Map<String, Object>> productCodes) {

        for(Map<String, Object> map : productCodes) {
            int productCode = Integer.parseInt(map.get("productCode").toString());
            adminProductService.insHollybamPick(productCode);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "홀리밤 픽이 추가되었습니다.");
        return response;
    }

    @PostMapping("/pick/delete")
    @ResponseBody
    public ResponseEntity<?> deleteHollybamPick(@RequestBody int pickCode) {
        if(adminProductService.deleteHollybamPick(pickCode) > 0){
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }
}