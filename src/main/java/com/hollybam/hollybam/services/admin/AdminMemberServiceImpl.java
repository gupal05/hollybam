package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminMemberDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminMemberServiceImpl implements IF_AdminMemberService {

    private final IF_AdminMemberDao adminMemberDao;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMemberList(Map<String, Object> searchParams) throws Exception {
        try {
            List<Map<String, Object>> members = adminMemberDao.selectMemberList(searchParams);
            log.info("관리자 회원 목록 조회 완료 - 조회 개수: {}", members.size());
            return members;

        } catch (Exception e) {
            log.error("관리자 회원 목록 조회 중 오류 발생", e);
            throw new Exception("회원 목록 조회에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getMemberCount(Map<String, Object> searchParams) throws Exception {
        try {
            int count = adminMemberDao.selectMemberCount(searchParams);
            log.info("관리자 회원 총 개수 조회 완료 - 총 개수: {}", count);
            return count;

        } catch (Exception e) {
            log.error("관리자 회원 총 개수 조회 중 오류 발생", e);
            throw new Exception("회원 개수 조회에 실패했습니다.", e);
        }
    }
}