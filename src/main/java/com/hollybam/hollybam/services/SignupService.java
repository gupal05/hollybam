package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_SignupDao;
import com.hollybam.hollybam.dto.MemberDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService implements IF_SignupService{
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
            return signupDao.signup(memberDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
