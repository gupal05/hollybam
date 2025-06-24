package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_CouponDao;
import com.hollybam.hollybam.dto.CouponDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService implements IF_CouponService {
    private final IF_CouponDao couponDao;

    public CouponService(IF_CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    @Override
    public void insertCoupon(CouponDto couponDto) {
        couponDao.insertCoupon(couponDto);
    }

    @Override
    public List<CouponDto> getUsePossibleCoupon(int memberId) {
        return couponDao.getUsePossibleCoupon(memberId);
    }
}
