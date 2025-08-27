package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.CouponDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_CouponDao {
    void insertCoupon(CouponDto couponDto);
    public List<CouponDto> getUsePossibleCoupon(int memberId);
    public int selectTotalCouponCount(int memberCode);
    public int selectCouponCount(int memberCode);
    public int selectUsedCouponCount(int memberCode);
    public int expirationCouponCount(int memberCode);
    /**
     * 회원의 쿠폰 목록 조회 (상태별)
     */
    List<Map<String, Object>> selectMemberCouponList(@Param("memCode") int memCode, @Param("status") String status);

    /**
     * 쿠폰 사용 처리
     */
    int useCoupon(@Param("couponMemberCode") int couponMemberCode, @Param("orderCode") int orderCode, @Param("discountAmount") int discountAmount);

    int getCouponMemberCode(@Param("memberCode") int memberCode,  @Param("couponCode") int couponCode);

    /**
     * 쿠폰 ID로 쿠폰 정보 조회
     */
    CouponDto findCouponByCouponId(@Param("couponId") String couponId);

    /**
     * 회원이 해당 쿠폰을 이미 가지고 있는지 확인
     */
    int checkMemberHasCoupon(@Param("memberCode") int memberCode, @Param("couponCode") int couponCode);

    /**
     * 회원에게 쿠폰 발급
     */
    int giveCouponToMember(@Param("memberCode") int memberCode, @Param("couponCode") int couponCode);

    List<Map<String, Object>> selectCouponList(@Param("search") String search,
                                               @Param("status") String status,
                                               @Param("size") int size,
                                               @Param("offset") int offset);

    int adminSelectCouponCount(@Param("search") String search,
                          @Param("status") String status);

    Map<String, Object> selectCouponStats();
}
