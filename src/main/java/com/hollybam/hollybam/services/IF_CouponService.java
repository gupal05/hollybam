package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.CouponDto;

import java.util.List;
import java.util.Map;

public interface IF_CouponService {
    void insertCoupon(CouponDto couponDto);
    public List<CouponDto> getUsePossibleCoupon(int memberId);
    public int selectTotalCouponCount(int memberCode);
    public int selectCouponCount(int memberCode);
    public int selectUsedCouponCount(int memberCode);
    public int expirationCouponCount(int memberCode);
    /**
     * 회원의 쿠폰 목록 조회 (상태별)
     * @param memCode 회원 코드
     * @param status 쿠폰 상태 ("all", "available", "used", "expired")
     * @return 쿠폰 목록 (쿠폰 정보 + 회원 쿠폰 정보 조인)
     */
    List<Map<String, Object>> selectMemberCouponList(int memCode, String status);

    /**
     * 쿠폰 사용 처리
     * @param couponMemberCode 회원 쿠폰 코드
     * @param orderCode 주문 코드
     * @return 성공 여부
     */
    boolean useCoupon(int couponMemberCode, int orderCode, int discountAmount);

    int getCouponMemberCode(int memberCode, int couponCode);

    /**
     * 쿠폰 번호로 쿠폰 등록
     * @param couponId 쿠폰 번호 (사용자가 입력한 쿠폰 ID)
     * @param memberCode 회원 코드
     * @return 등록 결과 맵 (success, message 포함)
     */
    Map<String, Object> registerCoupon(String couponId, int memberCode);
}
