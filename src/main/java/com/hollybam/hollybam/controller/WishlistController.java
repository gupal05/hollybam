package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.WishlistDto;
import com.hollybam.hollybam.services.IF_WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final IF_WishlistService wishlistService;

    /**
     * 위시리스트 페이지
     */
    @GetMapping
    public String wishlistPage(HttpSession session, Model model) {
        try {
            log.info("위시리스트 페이지 요청");

            // 세션에서 사용자 정보 가져오기
            MemberDto member = (MemberDto) session.getAttribute("member");
            Integer memCode = member != null ? member.getMemberCode() : null;
            String guestUuid = (String) session.getAttribute("guest_uuid");

            log.info("세션 정보 - memCode: {}, guestUuid: {}", memCode, guestUuid);

            List<WishlistDto> wishlistItems;
            int totalCount;

            if (memCode != null) {
                // 회원
                log.info("회원 위시리스트 조회 - memCode: {}", memCode);
                wishlistItems = wishlistService.getMemberWishlist(memCode);
                totalCount = wishlistService.getMemberWishlistCount(memCode);
            } else if (guestUuid != null) {
                // 비회원
                log.info("비회원 위시리스트 조회 - guestUuid: {}", guestUuid);
                Integer guestCode = wishlistService.getGuestCodeByUuid(guestUuid);
                log.info("guestCode 조회 결과: {}", guestCode);

                if (guestCode != null) {
                    wishlistItems = wishlistService.getGuestWishlist(guestCode);
                    totalCount = wishlistService.getGuestWishlistCount(guestCode);
                } else {
                    log.warn("guestCode를 찾을 수 없음");
                    wishlistItems = List.of();
                    totalCount = 0;
                }
            } else {
                // 로그인/인증 필요
                log.warn("로그인 또는 성인인증 필요");
                return "redirect:/intro";
            }

            log.info("위시리스트 아이템 수: {}, 총 개수: {}", wishlistItems.size(), totalCount);
            for(int i = 0; i < wishlistItems.size(); i++) {
                System.out.println(wishlistItems.get(i));
            }
            model.addAttribute("wishlistItems", wishlistItems);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("isLoggedIn", memCode != null);

            return "mypage/wishlist";

        } catch (Exception e) {
            log.error("위시리스트 페이지 로드 실패", e);
            model.addAttribute("error", "위시리스트를 불러오는 중 오류가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * 위시리스트 토글 (추가/제거)
     */
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @RequestParam int productCode,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("위시리스트 토글 요청 - productCode: {}", productCode);

            MemberDto member = (MemberDto) session.getAttribute("member");
            Integer memCode = member != null ? member.getMemberCode() : null;
            String guestUuid = (String) session.getAttribute("guest_uuid");

            boolean isInWishlist;

            if (memCode != null) {
                // 회원
                log.info("회원 위시리스트 토글 - memCode: {}", memCode);
                isInWishlist = wishlistService.toggleMemberWishlist(memCode, productCode);
            } else if (guestUuid != null) {
                // 비회원
                log.info("비회원 위시리스트 토글 - guestUuid: {}", guestUuid);
                Integer guestCode = wishlistService.getGuestCodeByUuid(guestUuid);
                if (guestCode == null) {
                    log.warn("guestCode를 찾을 수 없음 - guestUuid: {}", guestUuid);
                    response.put("success", false);
                    response.put("message", "성인인증이 필요합니다.");
                    return ResponseEntity.badRequest().body(response);
                }
                isInWishlist = wishlistService.toggleGuestWishlist(guestCode, productCode);
            } else {
                log.warn("인증 정보 없음");
                response.put("success", false);
                response.put("message", "로그인 또는 성인인증이 필요합니다.");
                response.put("requireAuth", true);
                return ResponseEntity.badRequest().body(response);
            }

            log.info("위시리스트 토글 완료 - productCode: {}, isInWishlist: {}", productCode, isInWishlist);

            response.put("success", true);
            response.put("isInWishlist", isInWishlist);
            response.put("message", isInWishlist ? "위시리스트에 추가되었습니다." : "위시리스트에서 제거되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("위시리스트 토글 실패 - productCode: {}", productCode, e);
            response.put("success", false);
            response.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 위시리스트 상태 확인 (여러 상품)
     */
    @PostMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistStatus(
            @RequestParam List<Integer> productCodes,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("위시리스트 상태 확인 요청 - productCodes: {}", productCodes);

            MemberDto member = (MemberDto) session.getAttribute("member");
            Integer memCode = member != null ? member.getMemberCode() : null;
            String guestUuid = (String) session.getAttribute("guest_uuid");

            log.info("세션 정보 - memCode: {}, guestUuid: {}", memCode, guestUuid);

            List<Integer> wishlistProductCodes;

            if (memCode != null) {
                log.info("회원 위시리스트 상태 조회");
                wishlistProductCodes = wishlistService.getWishlistProductCodes(memCode, null, productCodes);
            } else if (guestUuid != null) {
                log.info("비회원 위시리스트 상태 조회");
                Integer guestCode = wishlistService.getGuestCodeByUuid(guestUuid);
                log.info("guestCode: {}", guestCode);

                if (guestCode != null) {
                    wishlistProductCodes = wishlistService.getWishlistProductCodes(null, guestCode, productCodes);
                } else {
                    log.warn("guestCode를 찾을 수 없음");
                    wishlistProductCodes = List.of();
                }
            } else {
                log.warn("인증 정보 없음");
                wishlistProductCodes = List.of();
            }

            log.info("위시리스트에 있는 상품들: {}", wishlistProductCodes);

            response.put("success", true);
            response.put("wishlistProductCodes", wishlistProductCodes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("위시리스트 상태 확인 실패", e);
            response.put("success", false);
            response.put("message", "상태 확인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 위시리스트에서 상품 제거
     */
    @DeleteMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @RequestParam int productCode,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("위시리스트 제거 요청 - productCode: {}", productCode);

            MemberDto member = (MemberDto) session.getAttribute("member");
            Integer memCode = member != null ? member.getMemberCode() : null;
            String guestUuid = (String) session.getAttribute("guest_uuid");

            boolean removed;

            if (memCode != null) {
                log.info("회원 위시리스트 제거");
                removed = wishlistService.removeMemberWishlist(memCode, productCode);
            } else if (guestUuid != null) {
                log.info("비회원 위시리스트 제거");
                Integer guestCode = wishlistService.getGuestCodeByUuid(guestUuid);
                if (guestCode == null) {
                    log.warn("guestCode를 찾을 수 없음");
                    response.put("success", false);
                    response.put("message", "인증 정보를 찾을 수 없습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
                removed = wishlistService.removeGuestWishlist(guestCode, productCode);
            } else {
                log.warn("인증 정보 없음");
                response.put("success", false);
                response.put("message", "로그인 또는 성인인증이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("위시리스트 제거 완료 - productCode: {}, removed: {}", productCode, removed);

            response.put("success", removed);
            response.put("message", removed ? "위시리스트에서 제거되었습니다." : "제거할 상품을 찾을 수 없습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("위시리스트 제거 실패 - productCode: {}", productCode, e);
            response.put("success", false);
            response.put("message", "제거 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 위시리스트 개수 조회 (AJAX용)
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            Integer memCode = member != null ? member.getMemberCode() : null;
            String guestUuid = (String) session.getAttribute("guest_uuid");

            int count = 0;

            if (memCode != null) {
                count = wishlistService.getMemberWishlistCount(memCode);
            } else if (guestUuid != null) {
                Integer guestCode = wishlistService.getGuestCodeByUuid(guestUuid);
                if (guestCode != null) {
                    count = wishlistService.getGuestWishlistCount(guestCode);
                }
            }

            response.put("success", true);
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("위시리스트 개수 조회 실패", e);
            response.put("success", false);
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }
    }
}