package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.GuestDto;
import com.hollybam.hollybam.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface IF_SignupDao {
    public int dupCheckId(String memberId);
    public int signup(MemberDto memberDto);
    public void deleteGuestByDi(String uuid);
    public int getGuestCartCount(GuestDto guest);
    public int getGuestWishCount(GuestDto guest);
    public int getGuestOrderCount(GuestDto guest);
    public void updateGuestToMemberCart(int memberCode, int guestCode);
    public void updateGuestToMemberWishList(int memberCode, int guestCode);
    public void updateGuestToMemberOrder(int memberCode, int guestCode);
    public int getMemberCode(String memberId);
    public void insertSignupCoupon(int memberCode, int signupCouponCode);
    public int getSignupCouponCode();
    public int isRecodeSignup(String di);
    public String getMemberType(String di);
}
