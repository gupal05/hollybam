package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.PointDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IF_MypageDao {
    void updateMember(MemberDto memberDto);
    int selectMemberPoint(int memberCode);
    int selectMemberAddPoint(int memberCode);
    int selectMemberUsePoint(int memberCode);
    List<PointDto> selectPointHistory(@Param("memberCode") int memberCode,
                                             @Param("offset") int offset,
                                             @Param("size") int size);
    int selectPointHistoryCount(int memberCode);
    int getMemberPaidCount(int memberCode);
    int getGuestPaidCount(int guestCode);
    int getMemberShippedCount(int memberCode);
    int getGuestShippedCount(int guestCode);
    int getMemberDeliveredCount(int memberCode);
    int getGuestDeliveredCount(int guestCode);
    int getMemberCancelCount(int memberCode);
    int getGuestCancelCount(int guestCode);
}