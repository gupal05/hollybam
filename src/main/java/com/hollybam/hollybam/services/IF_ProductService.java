package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.CategoryDto;
import com.hollybam.hollybam.dto.ImageDto;
import com.hollybam.hollybam.dto.ProductDto;
import com.hollybam.hollybam.dto.ProductOptionDto;

import java.util.List;

public interface IF_ProductService {
    public int isProductCode(String code);
    public int insertProduct(CategoryDto categoryDto);
    public int getProductCode(ProductDto productDto);
    public int insertPrice(ProductDto productDto);
    public int insertImage(ProductDto productDto);
    public int insertProductOptions(ProductDto productDto);
    public List<ProductDto> selectBestProducts();
    public ProductDto getProductDetailInfo_first(String productId);
    public List<ImageDto> getProductInfoThumbnail(int productCode);
    public List<ImageDto> getProductInfoContent(int productCode);
    public List<ProductOptionDto> getProductOptions(int productCode);
}
