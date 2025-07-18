package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_LoginDao;
import com.hollybam.hollybam.dto.MemberDto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService implements IF_LoginService{
    private final IF_LoginDao loginDao;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginService(IF_LoginDao loginDao) {
        this.loginDao = loginDao;
    }

    @Override
    @Transactional
    public MemberDto getMemberInfo(String memberId){
        return loginDao.getMemberInfo(memberId);
    }

    public boolean login(MemberDto memberDto) {
        MemberDto dbMember = loginDao.getMemberInfo(memberDto.getMemberId());
        if (dbMember == null) {
            return false; // 회원 없음
        }

        // 평문 비밀번호(memberDto.getMemberPw())와 암호화된 비밀번호(dbMember.getMemberPw()) 비교
        boolean matches = passwordEncoder.matches(memberDto.getMemberPw(), dbMember.getMemberPw());

        if (matches) {
            return true; // 로그인 성공
        } else {
            return false; // 비밀번호 불일치
        }
    }

    public int isNaverMember(String naverId) {
        return loginDao.isNaverMember(naverId);
    }

    /**
     * 구글 회원 존재 여부 확인
     * @param googleId 구글 회원 ID (google_ prefix 포함)
     * @return 존재하면 1 이상, 없으면 0
     */
    public int isGoogleMember(String googleId) {
        return loginDao.isGoogleMember(googleId);
    }
}