package com.hollybam.hollybam.controller.adminController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.admin.IF_AdminMemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/member")
@RequiredArgsConstructor
@Slf4j
public class AdminMemberController {

    private final IF_AdminMemberService adminMemberService;

    /**
     * 관리자 회원 목록 페이지
     */
    @GetMapping("/list")
    public String adminMemberList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String loginType,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String hasPurchase,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            Model model, HttpSession session) {

        // 관리자 권한 확인
        if(session.getAttribute("member") != null){
            MemberDto mem = (MemberDto) session.getAttribute("member");
            if(!mem.getMemberRole().equals("admin")){
                return "redirect:/";
            } else {
                try {
                    // 검색 조건 설정 (Map으로 처리)
                    Map<String, Object> searchParams = Map.of(
                            "page", page,
                            "size", size,
                            "searchType", searchType != null ? searchType : "",
                            "searchKeyword", searchKeyword != null ? searchKeyword : "",
                            "loginType", loginType != null ? loginType : "",
                            "gender", gender != null ? gender : "",
                            "hasPurchase", hasPurchase != null ? hasPurchase : "",
                            "sortBy", sortBy,
                            "sortOrder", sortOrder,
                            "offset", (page - 1) * size
                    );

                    // 회원 목록 조회
                    List<Map<String, Object>> members = adminMemberService.getMemberList(searchParams);

                    // 총 개수 조회
                    int totalCount = adminMemberService.getMemberCount(searchParams);

                    // 🆕 통계 정보 계산 (람다식 대신 반복문 사용)
                    long purchaseCount = 0;
                    long maleCount = 0;
                    long femaleCount = 0;

                    for (Map<String, Object> member : members) {
                        String hasPurchases = (String) member.get("hasPurchase");
                        String genders = (String) member.get("gender");

                        if ("O".equals(hasPurchases)) {
                            purchaseCount++;
                        }
                        if ("남자".equals(genders)) {
                            maleCount++;
                        }
                        if ("여자".equals(genders)) {
                            femaleCount++;
                        }
                    }

                    // 페이징 정보 계산
                    int totalPages = (int) Math.ceil((double) totalCount / size);
                    int startPage = Math.max(1, page - 2);
                    int endPage = Math.min(totalPages, page + 2);

                    // 모델에 데이터 추가
                    model.addAttribute("members", members);
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", totalPages);
                    model.addAttribute("totalCount", totalCount);
                    model.addAttribute("startPage", startPage);
                    model.addAttribute("endPage", endPage);
                    model.addAttribute("size", size);
                    model.addAttribute("searchParams", searchParams);

                    // 🆕 통계 정보 추가
                    model.addAttribute("purchaseCount", purchaseCount);
                    model.addAttribute("maleCount", maleCount);
                    model.addAttribute("femaleCount", femaleCount);

                    log.info("관리자 회원 목록 조회 - 페이지: {}, 총 개수: {}", page, totalCount);

                } catch (Exception e) {
                    log.error("관리자 회원 목록 조회 중 오류 발생", e);
                    model.addAttribute("errorMessage", "회원 목록을 불러오는 중 오류가 발생했습니다.");
                }

                return "admin/member/list";
            }
        } else {
            return "redirect:/admin";
        }
    }
}