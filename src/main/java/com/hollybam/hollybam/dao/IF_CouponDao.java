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
    int useCoupon(@Param("couponMemberCode") int couponMemberCode, @Param("orderCode") int orderCode);
}
