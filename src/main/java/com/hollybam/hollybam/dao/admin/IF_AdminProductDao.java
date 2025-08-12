package com.hollybam.hollybam.dao.admin;

import com.hollybam.hollybam.controller.adminController.AdminProductController.AdminProductSearchDto;
import com.hollybam.hollybam.dto.CategoryDto;
import com.hollybam.hollybam.dto.ProductDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_AdminProductDao {

    /**
     * 관리자용 상품 목록 조회 (검색, 필터, 정렬 포함)
     * @param searchDto 검색 조건
     * @param offset 페이징 오프셋
     * @return 상품 목록
     */
    List<ProductDto> adminSelectProductList(@Param("search") AdminProductSearchDto searchDto, @Param("offset") int offset) throws Exception;

    /**
     * 관리자용 상품 총 개수 조회
     * @param searchDto 검색 조건
     * @return 총 개수
     */
    long adminSelectProductCount(@Param("search") AdminProductSearchDto searchDto) throws Exception;

    /**
     * 관리자용 모든 카테고리 조회
     * @return 카테고리 목록
     */
    List<CategoryDto> adminSelectAllCategories() throws Exception;

    /**
     * 관리자용 상품 상세 조회
     * @param productCode 상품 코드
     * @return 상품 상세 정보
     */
    ProductDto adminSelectProductDetail(@Param("productCode") int productCode) throws Exception;

    /**
     * 관리자용 상품 상태 업데이트
     * @param productCode 상품 코드
     * @param status 상태 (1: 활성, 0: 비활성, -1: 삭제)
     */
    void adminUpdateProductStatus(@Param("productCode") int productCode, @Param("status") int status) throws Exception;

    /**
     * 관리자용 상품 일괄 상태 업데이트
     * @param productCodes 상품 코드 목록
     * @param status 상태 (1: 활성, 0: 비활성, -1: 삭제)
     */
    void adminBulkUpdateProductStatus(@Param("productCodes") List<Integer> productCodes, @Param("status") int status) throws Exception;

    Map<String, Object> getProductInfo(@Param("productCode") int productCode);

    List<Map<String, Object>> getEditProductThumbnail(@Param("productCode") int productCode);

    List<Map<String, Object>> getEditProductDetail(@Param("productCode") int productCode);

    String getOptionName(@Param("productCode") int productCode);

    List<Map<String, Object>> getProductOption(@Param("productCode") int productCode);
}