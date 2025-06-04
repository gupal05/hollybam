package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.MemberDto;

public interface IF_SignupService {
    public int dupCheckId(String memberId);
    public int signup(MemberDto memberDto);
}
