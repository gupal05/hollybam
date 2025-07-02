package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.MemberDto;

public interface IF_SignupService {
    public int dupCheckId(String memberId);
    public int signup(MemberDto memberDto);
    public MemberDto getGuestInfo(String uuId);
    public void deleteGuestByDi(String uuid);
    public int getMemberCode(String memberId);
    public void insertSignupCoupon(int memberCode, int signupCouponCode);
    public int getSignupCouponCode();
    public int isRecodeSignup(String di);
    public String getMemberType(String di);
}
