package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.DiscountDto;
import com.hollybam.hollybam.dto.DiscountCodeUsageDto;

import java.util.List;
import java.util.Map;

public interface IF_DiscountService {
    void insertDiscount(DiscountDto discountDto);

    /**
     * 할인코드 검증 (🆕 중복 사용 여부 포함)
     * @param discountId 할인코드
     * @param orderAmount 주문금액
     * @param memCode 회원 코드 (비회원인 경우 null)
     * @return 검증 결과 (할인 정보 및 할인 금액)
     * @throws RuntimeException 검증 실패 시
     */
    Map<String, Object> validateDiscountCode(String discountId, Long orderAmount, Integer memCode) throws Exception;

    /**
     * 할인코드 조회
     * @param discountId 할인코드
     * @return 할인코드 정보
     */
    DiscountDto getDiscountByCode(String discountId) throws Exception;

    // ===== 🆕 할인코드 사용 내역 관련 메서드 추가 =====

    /**
     * 할인코드 사용 내역 저장
     * @param discountCode 할인코드 번호 (discount 테이블의 PK)
     * @param memCode 회원 코드
     * @throws Exception 저장 실패 시
     */
    void recordDiscountCodeUsage(Integer discountCode, Integer memCode) throws Exception;

    /**
     * 할인코드 사용 내역 저장
     * @param discountCode 할인코드 번호 (discount 테이블의 PK)
     * @param guestCode 회원 코드
     * @throws Exception 저장 실패 시
     */
    void recordDiscountCodeUsageForGuest(Integer discountCode, Integer guestCode) throws Exception;

    /**
     * 회원의 할인코드 사용 내역 조회
     * @param memCode 회원 코드
     * @return 사용 내역 목록
     */
    List<DiscountCodeUsageDto> getDiscountUsageHistory(Integer memCode) throws Exception;
}