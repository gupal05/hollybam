// IF_CouponDao.java
package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 쿠폰 DAO 인터페이스
 * 쿠폰 관련 데이터베이스 처리
 */
@Mapper
public interface IF_CouponDao {

    // ===============================
    // 쿠폰 마스터 관련
    // ===============================

    /**
     * 쿠폰 마스터 등록 (관리자만 가능)
     */
    int insertCouponMaster(CouponMasterDto couponMasterDto);

    /**
     * 쿠폰 마스터 수정 (관리자만 가능)
     */
    int updateCouponMaster(CouponMasterDto couponMasterDto);

    /**
     * 쿠폰 마스터 삭제 (관리자만 가능)
     */
    int deleteCouponMaster(@Param("couponMasterCode") Integer couponMasterCode);

    /**
     * 쿠폰 마스터 상세 조회
     */
    CouponMasterDto selectCouponMasterDetail(@Param("couponMasterCode") Integer couponMasterCode);

    /**
     * 활성화된 쿠폰 마스터 목록 조회
     */
    List<CouponMasterDto> selectActiveCouponMasterList(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 모든 쿠폰 마스터 목록 조회 (관리자용)
     */
    List<CouponMasterDto> selectAllCouponMasterList(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 발급 가능한 쿠폰 마스터 목록 조회
     */
    List<CouponMasterDto> selectIssuableCouponMasterList();

    /**
     * 쿠폰 마스터 총 개수 조회
     */
    int selectCouponMasterCount(@Param("isActive") Boolean isActive);

    /**
     * 쿠폰 발급 수량 증가
     */
    int increaseCouponIssuedQuantity(@Param("couponMasterCode") Integer couponMasterCode);

    // ===============================
    // 쿠폰 적용 상품 관련
    // ===============================

    /**
     * 쿠폰 적용 상품 등록
     */
    int insertCouponApplicableProducts(@Param("list") List<CouponApplicableProductDto> products);

    /**
     * 쿠폰 적용 상품 삭제
     */
    int deleteCouponApplicableProducts(@Param("couponMasterCode") Integer couponMasterCode);

    /**
     * 쿠폰 적용 상품 목록 조회
     */
    List<CouponApplicableProductDto> selectCouponApplicableProducts(@Param("couponMasterCode") Integer couponMasterCode);

    /**
     * 특정 상품에 적용 가능한 쿠폰 마스터 목록 조회
     */
    List<CouponMasterDto> selectApplicableCouponMastersForProduct(@Param("productCode") Integer productCode);

    // ===============================
    // 회원 쿠폰 관련
    // ===============================

    /**
     * 회원 쿠폰 발급
     */
    int insertMemberCoupon(MemberCouponDto memberCouponDto);

    /**
     * 회원 쿠폰 상태 변경 (사용/만료)
     */
    int updateMemberCouponStatus(@Param("memberCouponCode") Integer memberCouponCode,
                                 @Param("status") String status,
                                 @Param("orderCode") Integer orderCode);

    /**
     * 회원의 사용 가능한 쿠폰 목록 조회
     */
    List<MemberCouponDto> selectUsableMemberCoupons(@Param("memCode") Integer memCode);

    /**
     * 회원의 모든 쿠폰 목록 조회 (마이페이지용)
     */
    List<MemberCouponDto> selectMemberCoupons(@Param("memCode") Integer memCode,
                                              @Param("status") String status,
                                              @Param("offset") int offset,
                                              @Param("limit") int limit);

    /**
     * 쿠폰 코드로 회원 쿠폰 조회
     */
    MemberCouponDto selectMemberCouponByCouponCode(@Param("couponCode") String couponCode,
                                                   @Param("memCode") Integer memCode);

    /**
     * 회원의 쿠폰 개수 조회
     */
    int selectMemberCouponCount(@Param("memCode") Integer memCode, @Param("status") String status);

    /**
     * 특정 상품에 사용 가능한 회원 쿠폰 목록 조회
     */
    List<MemberCouponDto> selectUsableMemberCouponsForProduct(@Param("memCode") Integer memCode,
                                                              @Param("productCode") Integer productCode);

    /**
     * 장바구니 상품들에 사용 가능한 회원 쿠폰 목록 조회
     */
    List<MemberCouponDto> selectUsableMemberCouponsForCart(@Param("memCode") Integer memCode,
                                                           @Param("productCodes") List<Integer> productCodes);

    /**
     * 회원이 특정 쿠폰을 이미 발급받았는지 확인
     */
    int checkMemberCouponExists(@Param("memCode") Integer memCode,
                                @Param("couponMasterCode") Integer couponMasterCode);

    /**
     * 만료된 쿠폰 상태 업데이트 (배치용)
     */
    int updateExpiredCoupons();

    // ===============================
    // 쿠폰 사용 내역 관련
    // ===============================

    /**
     * 쿠폰 사용 내역 등록
     */
    int insertCouponUsageLog(CouponUsageLogDto couponUsageLogDto);

    /**
     * 회원의 쿠폰 사용 내역 조회
     */
    List<CouponUsageLogDto> selectMemberCouponUsageLog(@Param("memCode") Integer memCode,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);

    /**
     * 쿠폰 사용 통계 조회 (관리자용)
     */
    List<CouponUsageLogDto> selectCouponUsageStatistics(@Param("startDate") String startDate,
                                                        @Param("endDate") String endDate);

    // ===============================
    // 유틸리티
    // ===============================

    /**
     * 쿠폰 코드 중복 체크
     */
    int checkCouponCodeExists(@Param("couponCode") String couponCode);

    /**
     * 관리자 권한 확인
     */
    int isAdmin(@Param("memberCode") Integer memberCode);
}