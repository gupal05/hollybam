package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.CouponDto;

import java.util.List;

public interface IF_CouponService {
    void insertCoupon(CouponDto couponDto);
    public List<CouponDto> getUsePossibleCoupon(int memberId);
}
