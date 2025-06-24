package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.DiscountDto;

import java.util.Map;

public interface IF_DiscountService {
    void insertDiscount(DiscountDto discountDto);
    /**
     * 할인코드 검증
     * @param discountId 할인코드
     * @param orderAmount 주문금액
     * @return 검증 결과 (할인 정보 및 할인 금액)
     * @throws RuntimeException 검증 실패 시
     */
    Map<String, Object> validateDiscountCode(String discountId, Long orderAmount) throws Exception;

    /**
     * 할인코드 조회
     * @param discountId 할인코드
     * @return 할인코드 정보
     */
    DiscountDto getDiscountByCode(String discountId) throws Exception;
}
