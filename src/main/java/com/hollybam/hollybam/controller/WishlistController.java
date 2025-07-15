package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.GuestDto;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.WishlistDto;
import com.hollybam.hollybam.services.CouponService;
import com.hollybam.hollybam.services.MypageService;
import com.hollybam.hollybam.services.WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private MypageService mypageService;

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
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            log.info("세션 정보 - memCode: {}, guest: {}", memCode, guest);

            List<WishlistDto> wishlistItems;
            int totalCount;
            int couponCount = 0;

            if (memCode != null) {
                // 회원
                log.info("회원 위시리스트 조회 - memCode: {}", memCode);
                couponCount = couponService.selectCouponCount(memCode);
                wishlistItems = wishlistService.getMemberWishlist(memCode);
                totalCount = wishlistService.getMemberWishlistCount(memCode);
                int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
                String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);

                model.addAttribute("totalPoint", totalPoint);
                model.addAttribute("wishlistItems", wishlistItems);
                model.addAttribute("totalCount", totalCount);
                model.addAttribute("couponCount", couponCount);
                model.addAttribute("isLoggedIn", true);

                // 회원용 위시리스트 페이지로 이동
                return "mypage/wishlist";

            } else if (guest != null) {
                // 비회원
                log.info("비회원 위시리스트 조회 - guest: {}", guest);
                Integer guestCode = guest.getGuestCode();
                log.info("guestCode 조회 결과: {}", guestCode);

                if (guestCode != null) {
                    wishlistItems = wishlistService.getGuestWishlist(guestCode);
                    totalCount = wishlistService.getGuestWishlistCount(guestCode);
                } else {
                    log.warn("guestCode를 찾을 수 없음");
                    wishlistItems = List.of();
                    totalCount = 0;
                }

                model.addAttribute("wishlistItems", wishlistItems);
                model.addAttribute("totalCount", totalCount);
                model.addAttribute("isLoggedIn", false);

                // 비회원용 위시리스트 페이지로 이동
                return "mypage/wishlist";

            } else {
                // 로그인/인증 필요
                log.warn("로그인 또는 성인인증 필요");
                return "redirect:/intro";
            }

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
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            boolean isInWishlist;

            if (memCode != null) {
                // 회원
                log.info("회원 위시리스트 토글 - memCode: {}", memCode);
                isInWishlist = wishlistService.toggleMemberWishlist(memCode, productCode);
            } else if (guest != null) {
                // 비회원
                log.info("비회원 위시리스트 토글 - guest: {}", guest);
                int guestCode = guest.getGuestCode();
                if (guestCode == 0) {
                    log.warn("guestCode를 찾을 수 없음 - guest: {}", guest);
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
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            log.info("세션 정보 - memCode: {}, guest: {}", memCode, guest);

            List<Integer> wishlistProductCodes;

            if (memCode != null) {
                log.info("회원 위시리스트 상태 조회");
                wishlistProductCodes = wishlistService.getWishlistProductCodes(memCode, null, productCodes);
            } else if (guest != null) {
                log.info("비회원 위시리스트 상태 조회");
                Integer guestCode = guest.getGuestCode();
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
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            boolean removed;

            if (memCode != null) {
                log.info("회원 위시리스트 제거");
                removed = wishlistService.removeMemberWishlist(memCode, productCode);
            } else if (guest != null) {
                log.info("비회원 위시리스트 제거");
                int guestCode = guest.getGuestCode();
                if (guestCode == 0) {
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
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            int count = 0;

            if (memCode != null) {
                count = wishlistService.getMemberWishlistCount(memCode);
            } else if (guest != null) {
                int guestCode = guest.getGuestCode();
                if (guestCode != 0) {
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