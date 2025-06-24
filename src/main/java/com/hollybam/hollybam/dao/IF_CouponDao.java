package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.CouponDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IF_CouponDao {
    void insertCoupon(CouponDto couponDto);
    public List<CouponDto> getUsePossibleCoupon(int memberId);
}
