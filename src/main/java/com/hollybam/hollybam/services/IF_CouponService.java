package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.*;
import java.util.List;

/**
 * 쿠폰 서비스 인터페이스 (결제 연동 제외)
 */
public interface IF_CouponService {

    // ===============================
    // 관리자 기능
    // ===============================

    /**
     * 쿠폰 마스터 생성 (관리자만 가능)
     */
    boolean createCouponMaster(CouponMasterDto couponMasterDto, List<Integer> applicableProductCodes, Integer adminCode) throws Exception;

    /**
     * 쿠폰 마스터 수정 (관리자만 가능)
     */
    boolean updateCouponMaster(CouponMasterDto couponMasterDto, List<Integer> applicableProductCodes, Integer adminCode) throws Exception;

    /**
     * 쿠폰 마스터 삭제 (관리자만 가능)
     */
    boolean deleteCouponMaster(Integer couponMasterCode, Integer adminCode) throws Exception;

    /**
     * 회원에게 쿠폰 직접 발급 (관리자 기능)
     */
    boolean issueCouponToMember(Integer couponMasterCode, Integer memCode, Integer adminCode) throws Exception;

    /**
     * 대량 쿠폰 발급 (관리자 기능)
     */
    int issueCouponToMultipleMembers(Integer couponMasterCode, List<Integer> memCodes, Integer adminCode) throws Exception;

    // ===============================
    // 쿠폰 조회 기능
    // ===============================

    /**
     * 쿠폰 마스터 상세 조회
     */
    CouponMasterDto getCouponMasterDetail(Integer couponMasterCode);

    /**
     * 활성화된 쿠폰 마스터 목록 조회
     */
    List<CouponMasterDto> getActiveCouponMasterList(int page, int size);

    /**
     * 모든 쿠폰 마스터 목록 조회 (관리자용)
     */
    List<CouponMasterDto> getAllCouponMasterList(int page, int size);

    /**
     * 발급 가능한 쿠폰 목록 조회
     */
    List<CouponMasterDto> getIssuableCouponList();

    /**
     * 쿠폰 마스터 총 개수 조회
     */
    int getCouponMasterCount(Boolean isActive);

    // ===============================
    // 회원 쿠폰 기능
    // ===============================

    /**
     * 회원이 직접 쿠폰 발급받기
     */
    boolean issueCouponByMember(Integer couponMasterCode, Integer memCode) throws Exception;

    /**
     * 회원의 사용 가능한 쿠폰 목록 조회
     */
    List<MemberCouponDto> getMemberUsableCoupons(Integer memCode);

    /**
     * 회원의 모든 쿠폰 목록 조회 (마이페이지용)
     */
    List<MemberCouponDto> getMemberCoupons(Integer memCode, String status, int page, int size);

    /**
     * 회원의 쿠폰 개수 조회
     */
    int getMemberCouponCount(Integer memCode, String status);

    /**
     * 특정 상품에 사용 가능한 쿠폰 목록 조회
     */
    List<MemberCouponDto> getUsableCouponsForProduct(Integer memCode, Integer productCode);

    /**
     * 장바구니에 사용 가능한 쿠폰 목록 조회
     */
    List<MemberCouponDto> getUsableCouponsForCart(Integer memCode, List<Integer> productCodes);

    /**
     * 쿠폰 코드로 회원 쿠폰 조회
     */
    MemberCouponDto getMemberCouponByCouponCode(String couponCode, Integer memCode);

    // ===============================
    // 쿠폰 할인 계산 (결제 연동 없는 단순 계산)
    // ===============================

    /**
     * 쿠폰 할인 금액 미리 계산 (결제 전 확인용)
     */
    Integer calculateCouponDiscount(String couponCode, Integer memCode, List<Integer> productCodes, Integer orderAmount) throws Exception;

    /**
     * 쿠폰 사용 가능 여부 검증
     */
    boolean validateCouponUsage(String couponCode, Integer memCode, List<Integer> productCodes, Integer orderAmount) throws Exception;

    // ===============================
    // 통계 및 관리
    // ===============================

    /**
     * 만료된 쿠폰 정리 (배치 작업용)
     */
    int processExpiredCoupons();

    /**
     * 관리자 권한 확인
     */
    boolean isAdminUser(Integer memberCode);

    /**
     * 페이지네이션 계산
     */
    int calculateTotalPages(int totalCount, int size);
}