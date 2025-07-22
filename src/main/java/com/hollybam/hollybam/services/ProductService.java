package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_ProductDao;
import com.hollybam.hollybam.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService implements IF_ProductService{
    private final IF_ProductDao productDao;

    public ProductService(IF_ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    @Transactional
    public int insertProduct(CategoryDto categoryDto) {
        return productDao.insertProduct(categoryDto);
    }

    @Override
    @Transactional
    public int getProductCode(ProductDto productDto) {
        return productDao.getProductCode(productDto);
    }

    @Override
    @Transactional
    public int insertPrice(ProductDto productDto) {
        return productDao.insertPrice(productDto);
    }

    @Override
    @Transactional
    public int insertImage(ProductDto productDto) {
        return productDao.insertImage(productDto);
    }

    @Override
    @Transactional
    public int isProductCode(String code) {
        return productDao.isProductCode(code);
    }

    @Override
    @Transactional
    public List<ProductDto>selectBestProducts(){
        List<ProductDto> productList = productDao.selectBestProducts();
        return productDao.selectBestProducts();
    }

    @Override
    @Transactional
    public List<ProductDto>selectBestProductsForMobile(){
        List<ProductDto> productList = productDao.selectBestProductsForMobile();
        return productDao.selectBestProductsForMobile();
    }

    @Override
    @Transactional
    public List<ProductDto> selectNewProducts() {
        return productDao.selectNewProducts();
    }

    @Override
    @Transactional
    public ProductDto getProductDetailInfo_first(String productId) {
        return productDao.getProductDetailInfo_first(productId);
    }

    @Override
    @Transactional
    public Double getProductReviewAvg(int productCode){
        return productDao.getProductReviewAvg(productCode);
    }

    @Override
    @Transactional
    public int getProductReviewCount(int productCode){
        return productDao.getProductReviewCount(productCode);
    }

    @Override
    @Transactional
    public List<ImageDto> getProductInfoThumbnail(int productCode){
        return productDao.getProductInfoThumbnail(productCode);
    }

    @Override
    @Transactional
    public List<ImageDto> getProductInfoContent(int productCode){
        return productDao.getProductInfoContent(productCode);
    }

    @Override
    @Transactional
    public int insertProductOptions(ProductDto productDto) {
        return productDao.insertProductOptions(productDto);
    }

    @Override
    @Transactional
    public List<ProductOptionDto> getProductOptions(int productCode) {
        return productDao.getProductOptions(productCode);
    }

    @Override
    @Transactional
    public List<ProductDto> getProductsByCategoryCode(String categoryCode, String sort) {
        return productDao.selectProductsByCategoryCode(categoryCode, sort);
    }

    @Override
    @Transactional
    public List<ProductDto> getProductsByCategoryAndDetail(String categoryCode, String detailCode, String sort) {
        return productDao.selectProductsByCategoryAndDetail(categoryCode, detailCode, sort);
    }

    @Override
    @Transactional
    public ProductDto getProductByCode(int productCode) {
        return productDao.selectProductByCode(productCode);
    }

    @Override
    @Transactional
    public PriceDto getProductPrice(int productCode) {
        return productDao.selectProductPrice(productCode);
    }

    @Override
    @Transactional
    public ProductOptionDto getProductOption(int optionCode) {
        return productDao.selectProductOption(optionCode);
    }

    @Override
    @Transactional
    public ImageDto getProductTitleImage(int productCode) {
        return productDao.selectProductTitleImage(productCode);
    }

    @Override
    @Transactional
    public int getWishCount(int productCode){
        return productDao.getWishCount(productCode);
    }

    @Override
    @Transactional
    public List<ProductDto> getProductsByCategoryAndDetailWithPaging(String categoryCode, String detailCode, String sort, int offset, int size){
        return productDao.selectProductsByCategoryAndDetailWithPaging(categoryCode, detailCode, sort, offset, size);
    }

    @Override
    @Transactional
    public List<ProductDto> getProductsByCategoryCodeWithPaging(String categoryCode, String sort, int offset, int size){
        return productDao.selectProductsByCategoryCodeWithPaging(categoryCode, sort, offset, size);
    }

    @Override
    @Transactional
    public int getProductCountByCategoryAndDetail(String categoryCode, String detailCode){
        return productDao.selectProductCountByCategoryAndDetail(categoryCode, detailCode);
    }

    @Override
    @Transactional
    public int getProductCountByCategory(String categoryCode){
        return productDao.selectProductCountByCategory(categoryCode);
    }

}
