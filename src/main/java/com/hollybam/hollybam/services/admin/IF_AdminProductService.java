package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.controller.adminController.AdminProductController.AdminProductSearchDto;
import com.hollybam.hollybam.controller.adminController.AdminProductController.AdminProductListResultDto;
import com.hollybam.hollybam.dto.CategoryDto;
import com.hollybam.hollybam.dto.ProductDto;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IF_AdminProductService {

    /**
     * 관리자용 상품 목록 조회 (검색, 필터, 페이징 포함)
     * @param searchDto 검색 조건
     * @return 상품 목록과 총 개수
     */
    AdminProductListResultDto adminGetProductList(AdminProductSearchDto searchDto) throws Exception;

    /**
     * 관리자용 모든 카테고리 조회
     * @return 카테고리 목록
     */
    List<CategoryDto> adminGetAllCategories() throws Exception;

    /**
     * 관리자용 상품 상태 토글 (활성화/비활성화)
     * @param productCode 상품 코드
     */
    void adminToggleProductStatus(int productCode) throws Exception;

    /**
     * 관리자용 상품 삭제 (소프트 삭제)
     * @param productCode 상품 코드
     */
    void adminDeleteProduct(int productCode) throws Exception;

    /**
     * 관리자용 상품 상세 조회
     * @param productCode 상품 코드
     * @return 상품 상세 정보
     */
    ProductDto adminGetProductDetail(int productCode) throws Exception;

    /**
     * 관리자용 상품 총 개수 조회
     * @param searchDto 검색 조건
     * @return 총 개수
     */
    long adminGetProductCount(AdminProductSearchDto searchDto) throws Exception;

    /**
     * 관리자용 상품 목록 Excel 파일 생성
     * @param products 상품 목록
     * @param response HTTP 응답 객체
     */
    void adminCreateProductExcel(List<ProductDto> products, HttpServletResponse response) throws Exception;

    /**
     * 관리자용 상품 일괄 상태 변경
     * @param productCodes 상품 코드 목록
     * @param status 변경할 상태
     */
    void adminBulkUpdateProductStatus(List<Integer> productCodes, int status) throws Exception;

    Map<String, Object> getProductInfo(int productCode);

    List<Map<String, Object>> getEditProductThumbnail(@Param("productCode") int productCode);

    List<Map<String, Object>> getEditProductDetail(@Param("productCode") int productCode);

    String getOptionName(@Param("productCode") int productCode);

    List<Map<String, Object>> getProductOption(@Param("productCode") int productCode);

    int getPickCount();

    List<Map<String, Object>> getPickList();

    List<Map<String, Object>> getProductList();

    List<Map<String, Object>> searchForPickProducts(String keyword, String categoryCode, String cateDetailCode);

    void insHollybamPick(int productCode);

    int deleteHollybamPick(int pickCode);
}