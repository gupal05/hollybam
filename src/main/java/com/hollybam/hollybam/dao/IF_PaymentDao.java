package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface IF_PaymentDao {

    // 장바구니 관련
    List<CartDto> selectCartItemsWithDetails(@Param("cartCodes") List<Integer> cartCodes);
    /**
     * 상품 가격 정보 조회
     */
    PriceDto selectProductPrice(int productCode);

    /**
     * 상품 옵션 정보 조회
     */
    ProductOptionDto selectProductOption(int optionCode);
    void insertPaymentLog(PaymentLogDto paymentLogDto);
}