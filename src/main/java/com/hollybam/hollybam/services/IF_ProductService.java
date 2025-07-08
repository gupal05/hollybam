package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.*;

import java.util.List;

public interface IF_ProductService {
    public int isProductCode(String code);
    public int insertProduct(CategoryDto categoryDto);
    public int getProductCode(ProductDto productDto);
    public int insertPrice(ProductDto productDto);
    public int insertImage(ProductDto productDto);
    public int insertProductOptions(ProductDto productDto);
    public List<ProductDto> selectBestProducts();
    public List<ProductDto> selectBestProductsForMobile();
    public List<ProductDto> selectNewProducts();
    public ProductDto getProductDetailInfo_first(String productId);
    public List<ImageDto> getProductInfoThumbnail(int productCode);
    public List<ImageDto> getProductInfoContent(int productCode);
    public List<ProductOptionDto> getProductOptions(int productCode);
    List<ProductDto> getProductsByCategoryCode(String categoryCode, String sort);
    List<ProductDto> getProductsByCategoryAndDetail(String categoryCode, String detailCode, String sort);
    /**
     * 상품 코드로 상품 정보 조회
     */
    ProductDto getProductByCode(int productCode);
    /**
     * 상품 코드로 가격 정보 조회
     */
    PriceDto getProductPrice(int productCode);

    /**
     * 옵션 코드로 특정 옵션 정보 조회
     */
    ProductOptionDto getProductOption(int optionCode);

    /**
     * 상품 대표 이미지 조회
     */
    ImageDto getProductTitleImage(int productCode);
}
