package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.OrderListDto;
import com.hollybam.hollybam.dto.PointDto;
import java.util.List;

public interface IF_MypageService {
    public void updateMember(MemberDto memberDto);
    public int selectMemberPoint(int memberCode);
    public int selectMemberAddPoint(int memberCode);
    public int selectMemberUsePoint(int memberCode);
    public List<PointDto> selectPointHistory(int memberCode, int page, int size);
    public int selectPointHistoryCount(int memberCode);
    int getMemberPaidCount(int memberCode);
    int getGuestPaidCount(int guestCode);
    int getMemberShippedCount(int memberCode);
    int getGuestShippedCount(int guestCode);
    int getMemberDeliveredCount(int memberCode);
    int getGuestDeliveredCount(int guestCode);
    int getMemberCancelCount(int memberCode);
    int getGuestCancelCount(int guestCode);
    List<OrderListDto> selectOrderDetailByOrderCode(int orderCode);
}