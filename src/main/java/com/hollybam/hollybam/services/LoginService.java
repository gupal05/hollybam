package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_LoginDao;
import com.hollybam.hollybam.dto.MemberDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService implements IF_LoginService{
    private final IF_LoginDao loginDao;

    public LoginService(IF_LoginDao loginDao) {
        this.loginDao = loginDao;
    }

    @Override
    @Transactional
    public int login(MemberDto memberDto){
        return loginDao.login(memberDto);
    }

    @Override
    @Transactional
    public MemberDto getMemberInfo(String memberId){
        return loginDao.getMemberInfo(memberId);
    }
}
