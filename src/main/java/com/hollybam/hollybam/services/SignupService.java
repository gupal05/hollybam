package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_SignupDao;
import com.hollybam.hollybam.dto.MemberDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService implements IF_SignupService{
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final IF_SignupDao signupDao;

    public SignupService(IF_SignupDao signupDao) {
        this.signupDao = signupDao;
    }

    @Override
    @Transactional
    public int dupCheckId(String memberId){
        try {
            return signupDao.dupCheckId(memberId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public int signup(MemberDto memberDto) {
        try {
            String encodedPw = passwordEncoder.encode(memberDto.getMemberPw());
            memberDto.setMemberPw(encodedPw);
            return signupDao.signup(memberDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public MemberDto getGuestInfo(String uuId){
        return signupDao.getGuestInfo(uuId);
    }

    @Override
    @Transactional
    public void deleteGuestByDi(String uuid){
        signupDao.deleteGuestByDi(uuid);
    }

    @Override
    public int getMemberCode(String memberId){
        return signupDao.getMemberCode(memberId);
    }

    @Override
    public int getSignupCouponCode() {
        return signupDao.getSignupCouponCode();
    }

    @Override
    @Transactional
    public void insertSignupCoupon(int memberCode, int signupCouponCode){
        signupDao.insertSignupCoupon(memberCode, signupCouponCode);
    }

    @Override
    public int isRecodeSignup(String di){
        return signupDao.isRecodeSignup(di);
    }
}
