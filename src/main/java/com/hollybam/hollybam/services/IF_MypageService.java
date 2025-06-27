package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.MemberDto;

public interface IF_MypageService {
    public void updateMember(MemberDto memberDto);
    public int selectMemberPoint(int memberCode);
    public int selectMemberAddPoint(int memberCode);
    public int selectMemberUsePoint(int memberCode);
}
