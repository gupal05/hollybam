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

    // NoticeController.java의 "관리자 전용 기능들" 부분에 추가할 메서드들

// ===========================================
// 관리자 전용 기능들
// ===========================================

    /**
     * 관리자 공지사항 목록 페이지
     * @param session HTTP 세션
     * @param page 페이지 번호 (기본값: 1)
     * @param size 페이지 크기 (기본값: 10)
     * @return 관리자용 공지사항 목록 페이지
     */
    @GetMapping("/admin/list")
    public ModelAndView adminNoticeList(HttpSession session,
                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size) {

        ModelAndView mav = new ModelAndView();

        try {
            // 관리자 권한 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !noticeService.isAdminUser(member.getMemberCode())) {
                mav.addObject("error", "관리자만 접근할 수 있습니다.");
                mav.setViewName("error/error");
                return mav;
            }

            // 모든 공지사항 목록 조회 (관리자용)
            List<NoticeDto> noticeList = noticeService.getAllNoticeList(page, size);
            int totalCount = noticeService.getAllNoticeCount();
            int totalPages = noticeService.calculateTotalPages(totalCount, page, size);

            mav.addObject("noticeList", noticeList);
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", totalPages);
            mav.addObject("totalCount", totalCount);
            mav.addObject("pageSize", size);

            mav.setViewName("admin/notice/adminNoticeList");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "공지사항 목록을 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 공지사항 작성 페이지 (관리자만 가능)
     * @param session HTTP 세션
     * @return 공지사항 작성 페이지
     */
    @GetMapping("/admin/create")
    public ModelAndView createNoticeForm(HttpSession session) {
        ModelAndView mav = new ModelAndView();

        try {
            // 관리자 권한 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !noticeService.isAdminUser(member.getMemberCode())) {
                mav.addObject("error", "관리자만 접근할 수 있습니다.");
                mav.setViewName("error/error");
                return mav;
            }

            // 빈 공지사항 객체 전달 (폼 바인딩용) - 기본값 설정
            NoticeDto notice = new NoticeDto();
            notice.setIsImportant(false);  // 기본값: 일반공지
            notice.setIsActive(true);      // 기본값: 활성화

            mav.addObject("notice", notice);
            mav.setViewName("admin/notice/createNotice");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "페이지를 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 공지사항 등록 처리 (관리자만 가능)
     * @param noticeDto 공지사항 정보
     * @param session HTTP 세션
     * @return 등록 결과 응답
     */
    @PostMapping("/admin/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createNotice(
            @ModelAttribute NoticeDto noticeDto,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 관리자 권한 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !noticeService.isAdminUser(member.getMemberCode())) {
                response.put("success", false);
                response.put("message", "관리자만 접근할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }

            // 공지사항 등록
            boolean success = noticeService.createNotice(noticeDto, member.getMemberCode());

            if (success) {
                response.put("success", true);
                response.put("message", "공지사항이 등록되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "공지사항 등록에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 공지사항 수정 페이지 (관리자만 가능)
     * @param noticeCode 공지사항 코드
     * @param session HTTP 세션
     * @return 공지사항 수정 페이지
     */
    @GetMapping("/admin/edit/{noticeCode}")
    public ModelAndView editNoticeForm(@PathVariable Integer noticeCode, HttpSession session) {
        ModelAndView mav = new ModelAndView();

        try {
            // 관리자 권한 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !noticeService.isAdminUser(member.getMemberCode())) {
                mav.addObject("error", "관리자만 접근할 수 있습니다.");
                mav.setViewName("error/error");
                return mav;
            }

            // 공지사항 조회
            NoticeDto notice = noticeService.getNoticeDetail(noticeCode);
            if (notice == null) {
                mav.addObject("error", "존재하지 않는 공지사항입니다.");
                mav.setViewName("error/error");
                return mav;
            }

            mav.addObject("notice", notice);
            mav.setViewName("admin/notice/editNotice");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "페이지를 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 공지사항 수정 처리 (관리자만 가능)
     * @param noticeCode 공지사항 코드
     * @param noticeDto 수정할 공지사항 정보
     * @param session HTTP 세션
     * @return 수정 결과 응답
     */
    @PostMapping("/admin/edit/{noticeCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateNotice(
            @PathVariable Integer noticeCode,
            @ModelAttribute NoticeDto noticeDto,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 관리자 권한 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !noticeService.isAdminUser(member.getMemberCode())) {
                response.put("success", false);
                response.put("message", "관리자만 접근할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }

            // 공지사항 코드 설정
            noticeDto.setNoticeCode(noticeCode);

            // 공지사항 수정
            boolean success = noticeService.updateNotice(noticeDto, member.getMemberCode());

            if (success) {
                response.put("success", true);
                response.put("message", "공지사항이 수정되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "공지사항 수정에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 공지사항 삭제 처리 (관리자만 가능)
     * @param noticeCode 삭제할 공지사항 코드
     * @param session HTTP 세션
     * @return 삭제 결과 응답
     */
    @DeleteMapping("/admin/delete/{noticeCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotice(
            @PathVariable Integer noticeCode,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 관리자 권한 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !noticeService.isAdminUser(member.getMemberCode())) {
                response.put("success", false);
                response.put("message", "관리자만 접근할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }

            // 공지사항 삭제
            boolean success = noticeService.deleteNotice(noticeCode, member.getMemberCode());

            if (success) {
                response.put("success", true);
                response.put("message", "공지사항이 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "공지사항 삭제에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 공지사항 활성화/비활성화 토글 (관리자만 가능)
     * @param noticeCode 공지사항 코드
     * @param isActive 활성화 여부
     * @param session HTTP 세션
     * @return 토글 결과 응답
     */
    @PostMapping("/admin/toggle/{noticeCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleNoticeActive(
            @PathVariable Integer noticeCode,
            @RequestParam Boolean isActive,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 관리자 권한 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !noticeService.isAdminUser(member.getMemberCode())) {
                response.put("success", false);
                response.put("message", "관리자만 접근할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }

            // 공지사항 활성화/비활성화 토글
            boolean success = noticeService.toggleNoticeActive(noticeCode, isActive, member.getMemberCode());

            if (success) {
                response.put("success", true);
                response.put("message", isActive ? "공지사항이 활성화되었습니다." : "공지사항이 비활성화되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "공지사항 상태 변경에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

}