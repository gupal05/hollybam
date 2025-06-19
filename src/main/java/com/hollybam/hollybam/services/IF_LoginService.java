package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.MemberDto;

public interface IF_LoginService {
    public MemberDto getMemberInfo(String memberId);
}
