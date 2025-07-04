package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.PointDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IF_MypageDao {
    public void updateMember(MemberDto memberDto);
    public int selectMemberPoint(int memberCode);
    public int selectMemberAddPoint(int memberCode);
    public int selectMemberUsePoint(int memberCode);
    public List<PointDto> selectPointHistory(@Param("memberCode") int memberCode,
                                             @Param("offset") int offset,
                                             @Param("size") int size);
    public int selectPointHistoryCount(int memberCode);
}