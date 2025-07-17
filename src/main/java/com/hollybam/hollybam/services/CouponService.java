package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_CouponDao;
import com.hollybam.hollybam.dto.CouponDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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

    @Override
    public int selectCouponCount(int memberCode) {
        return couponDao.selectCouponCount(memberCode);
    }

    @Override
    public int selectTotalCouponCount(int memberCode){
        return couponDao.selectTotalCouponCount(memberCode);
    }

    @Override
    public int selectUsedCouponCount(int memberCode){
        return couponDao.selectUsedCouponCount(memberCode);
    }

    @Override
    public int expirationCouponCount(int memberCode){
        return couponDao.expirationCouponCount(memberCode);
    }

    @Override
    public List<Map<String, Object>> selectMemberCouponList(int memCode, String status) {
        return couponDao.selectMemberCouponList(memCode, status);
    }

    @Override
    @Transactional
    public boolean useCoupon(int couponMemberCode, int orderCode) {
        try {
            return couponDao.useCoupon(couponMemberCode, orderCode) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public int getCouponMemberCode(int memberCode, int couponCode){
        return couponDao.getCouponMemberCode(memberCode, couponCode);
    }
}
