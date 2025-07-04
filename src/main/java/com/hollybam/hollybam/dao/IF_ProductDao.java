package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.CategoryDto;
import com.hollybam.hollybam.dto.ImageDto;
import com.hollybam.hollybam.dto.ProductDto;
import com.hollybam.hollybam.dto.ProductOptionDto;
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
}
