package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_MypageDao;
import com.hollybam.hollybam.dao.IF_PaymentDao;
import com.hollybam.hollybam.dto.MemberDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MypageService implements IF_MypageService {
    @Autowired
    private IF_MypageDao mypageDao;

    @Override
    @Transactional
    public void updateMember(MemberDto memberDto) {
        mypageDao.updateMember(memberDto);
    }
}
