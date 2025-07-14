package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_MypageDao;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.OrderListDto;
import com.hollybam.hollybam.dto.PointDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MypageService implements IF_MypageService {
    @Autowired
    private IF_MypageDao mypageDao;

    @Override
    @Transactional
    public void updateMember(MemberDto memberDto) {
        mypageDao.updateMember(memberDto);
    }

    @Override
    public int selectMemberPoint(int memberCode) {
        return mypageDao.selectMemberPoint(memberCode);
    }

    @Override
    public int selectMemberAddPoint(int memberCode){
        return mypageDao.selectMemberAddPoint(memberCode);
    }

    @Override
    public int selectMemberUsePoint(int memberCode){
        return mypageDao.selectMemberUsePoint(memberCode);
    }

    @Override
    public List<PointDto> selectPointHistory(int memberCode, int page, int size) {
        int offset = (page - 1) * size;
        return mypageDao.selectPointHistory(memberCode, offset, size);
    }

    @Override
    public int selectPointHistoryCount(int memberCode) {
        return mypageDao.selectPointHistoryCount(memberCode);
    }

    @Override
    @Transactional
    public int getMemberPaidCount(int memberCode){
        return mypageDao.getMemberPaidCount(memberCode);
    }
    @Override
    @Transactional
    public int getGuestPaidCount(int guestCode){
        return mypageDao.getGuestPaidCount(guestCode);
    }
    @Override
    @Transactional
    public int getMemberShippedCount(int memberCode){
        return mypageDao.getMemberShippedCount(memberCode);
    }
    @Override
    @Transactional
    public int getGuestShippedCount(int guestCode){
        return mypageDao.getGuestShippedCount(guestCode);
    }
    @Override
    @Transactional
    public int getMemberDeliveredCount(int memberCode){
        return mypageDao.getMemberDeliveredCount(memberCode);
    }
    @Override
    @Transactional
    public int getGuestDeliveredCount(int guestCode){
        return mypageDao.getGuestDeliveredCount(guestCode);
    }
    @Override
    @Transactional
    public int getMemberCancelCount(int memberCode){
        return mypageDao.getMemberCancelCount(memberCode);
    }
    @Override
    @Transactional
    public int getGuestCancelCount(int guestCode){
        return mypageDao.getGuestCancelCount(guestCode);
    }

    @Override
    @Transactional
    public List<OrderListDto> selectOrderDetailByOrderCode(int orderCode){
        return mypageDao.selectOrderDetailByOrderCode(orderCode);
    }
}