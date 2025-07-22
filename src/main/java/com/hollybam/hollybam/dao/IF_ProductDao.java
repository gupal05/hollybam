package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IF_ProductDao {
    public int isProductCode(String code);
    public int insertProduct(CategoryDto categoryDto);
    public int getProductCode(ProductDto productDto);
    public int insertPrice(ProductDto productDto);
    public int insertImage(ProductDto productDto);
    public int insertProductOptions(ProductDto productDto);
    public List<ProductDto> getProductList();
    public List<ProductDto> selectBestProducts();
    public List<ProductDto> selectBestProductsForMobile();
    public List<ProductDto> selectNewProducts();
    public ProductDto getProductDetailInfo_first(String productId);
    public List<ImageDto> getProductInfoThumbnail(int productCode);
    public List<ImageDto> getProductInfoContent(int productCode);
    public List<ProductOptionDto> getProductOptions(int productCode);
    List<ProductDto> selectProductsByCategoryCode(String categoryCode, String sort);
    List<ProductDto> selectProductsByCategoryAndDetail(String categoryCode, String detailCode, String sort);
    /**
     * 상품 코드로 상품 정보 조회
     */
    ProductDto selectProductByCode(int productCode);

    /**
     * 상품 가격 정보 조회
     */
    PriceDto selectProductPrice(int productCode);

    /**
     * 옵션 코드로 특정 옵션 정보 조회
     */
    ProductOptionDto selectProductOption(int optionCode);

    /**
     * 상품 대표 이미지 조회
     */
    ImageDto selectProductTitleImage(int productCode);

    int getWishCount(int productCode);

    Double getProductReviewAvg(int productCode);

    int getProductReviewCount(int productCode);

    List<ProductDto> selectProductsByCategoryAndDetailWithPaging(@Param("categoryCode") String categoryCode, @Param("detailCode") String detailCode,
                                                                 @Param("sort") String sort, @Param("offset") int offset,
                                                                 @Param("size") int size);

    List<ProductDto> selectProductsByCategoryCodeWithPaging(@Param("categoryCode") String categoryCode,
                                                                 @Param("sort") String sort, @Param("offset") int offset, @Param("size") int size);

    int selectProductCountByCategoryAndDetail(@Param("categoryCode") String categoryCode, @Param("detailCode") String detailCode);

    int selectProductCountByCategory(@Param("categoryCode") String categoryCode);
}
