package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.MemberDto;

import java.util.Map;

public interface IF_LoginService {
    public MemberDto getMemberInfo(String memberId);
    public int isNaverMember(String naverId);
}
