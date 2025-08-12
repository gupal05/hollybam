package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.NoticeDto;
import com.hollybam.hollybam.services.NoticeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 공지사항 컨트롤러
 * 관리자만 작성 가능한 공지사항 관련 요청 처리
 */
@Controller
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 공지사항 목록 페이지 (사용자용)
     * @param page 페이지 번호 (기본값: 1)
     * @param size 페이지 크기 (기본값: 10)
     * @return 공지사항 목록 페이지
     */
    @GetMapping("/list")
    public ModelAndView noticeList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        ModelAndView mav = new ModelAndView();

        try {
            // 활성화된 공지사항 목록 조회
            List<NoticeDto> noticeList = noticeService.getActiveNoticeList(page, size);
            int totalCount = noticeService.getActiveNoticeCount();
            int totalPages = noticeService.calculateTotalPages(totalCount, page, size);

            // 중요공지 목록 조회
            List<NoticeDto> importantNotices = noticeService.getImportantNoticeList();

            mav.addObject("noticeList", noticeList);
            mav.addObject("importantNotices", importantNotices);
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", totalPages);
            mav.addObject("totalCount", totalCount);
            mav.addObject("pageSize", size);

            mav.setViewName("notice/noticeList");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "공지사항 목록을 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 공지사항 상세 페이지
     * @param noticeCode 공지사항 코드
     * @return 공지사항 상세 페이지
     */
    @GetMapping("/detail/{noticeCode}")
    public ModelAndView noticeDetail(@PathVariable Integer noticeCode) {
        ModelAndView mav = new ModelAndView();

        try {
            NoticeDto notice = noticeService.getNoticeDetail(noticeCode);

            if (notice == null) {
                mav.addObject("error", "존재하지 않는 공지사항입니다.");
                mav.setViewName("error/error");
                return mav;
            }

            // 비활성화된 공지사항은 관리자만 볼 수 있음
            if (!notice.getIsActive()) {
                mav.addObject("error", "비활성화된 공지사항입니다.");
                mav.setViewName("error/error");
                return mav;
            }

            mav.addObject("notice", notice);
            mav.setViewName("notice/noticeDetail");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "공지사항을 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }
}