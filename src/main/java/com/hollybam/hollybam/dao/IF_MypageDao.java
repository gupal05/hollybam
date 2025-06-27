package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IF_MypageDao {
    public void updateMember(MemberDto memberDto);
    public int selectMemberPoint(int memberCode);
    public int selectMemberAddPoint(int memberCode);
    public int selectMemberUsePoint(int memberCode);
}
