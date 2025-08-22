package com.hollybam.hollybam.controller.reviewController;

import com.hollybam.hollybam.dto.*;
import com.hollybam.hollybam.services.IF_OrderService;
import com.hollybam.hollybam.services.IF_ReviewService;
import com.hollybam.hollybam.util.S3Uploader;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/review")
public class ReviewController {
    @Autowired
    private IF_ReviewService reviewService;
    @Autowired
    private IF_OrderService orderService;

    /**
     * 리뷰 목록 페이지 (초기 로드 시 사용자별 좋아요 상태 포함)
     */
    @GetMapping("")
    public String review(HttpSession session, Model model){
        // 현재 사용자 정보 조회
        Integer memCode = null;
        Integer guestCode = null;

        Object memberObj = session.getAttribute("member");
        Object guestObj = session.getAttribute("guest");

        if (memberObj != null) {
            MemberDto member = (MemberDto) memberObj;
            memCode = member.getMemberCode();
        } else if (guestObj != null) {
            GuestDto guest = (GuestDto) guestObj;
            guestCode = guest.getGuestCode();
        }

        // 초기 포토리뷰 데이터 로드 (첫 페이지, 최신순)
        List<Map<String, Object>> reviewList = reviewService.getPhotoReviews("latest", 1, 20, null, memCode, guestCode);
        Map<String,Object> map = reviewService.getReviewCount(null);

        model.addAttribute("reviewList", reviewList);
        model.addAttribute("photoReviews", map.get("photoReviews"));
        model.addAttribute("textReviews", map.get("textReviews"));

        return "review/reviewList";
    }

    /**
     * AJAX로 리뷰 데이터 조회 (탭 전환, 정렬, 필터링, 페이지네이션)
     */
    @GetMapping("/api/reviews")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReviews(
            @RequestParam(value = "type", defaultValue = "photo") String type,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "rating", required = false) Integer rating,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 현재 사용자 정보 조회
            Integer memCode = null;
            Integer guestCode = null;

            Object memberObj = session.getAttribute("member");
            Object guestObj = session.getAttribute("guest");

            if (memberObj != null) {
                MemberDto member = (MemberDto) memberObj;
                memCode = member.getMemberCode();
            } else if (guestObj != null) {
                GuestDto guest = (GuestDto) guestObj;
                guestCode = guest.getGuestCode();
            }

            List<Map<String, Object>> reviewList = new ArrayList<>();

            // 리뷰 타입에 따라 데이터 조회
            if ("photo".equals(type)) {
                reviewList = reviewService.getPhotoReviews(sort, page, size, rating, memCode, guestCode);
            } else if ("text".equals(type)) {
                reviewList = reviewService.getTextReviews(sort, page, size, rating, memCode, guestCode);
            }

            // 리뷰 카운트 정보 (필터링 적용)
            Map<String, Object> reviewCount = reviewService.getReviewCount(rating);

            response.put("success", true);
            response.put("reviewList", reviewList);
            response.put("photoReviews", reviewCount.get("photoReviews"));
            response.put("textReviews", reviewCount.get("textReviews"));
            response.put("currentType", type);
            response.put("currentSort", sort);
            response.put("currentPage", page);
            response.put("currentRating", rating);
            response.put("hasMore", reviewList.size() == size); // 더 있는지 여부

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "리뷰 데이터를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 리뷰 좋아요 토글 API
     */
    @PostMapping("/api/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleReviewLike(
            @RequestParam("reviewCode") int reviewCode,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 현재 사용자 정보 조회
            Integer memCode = null;
            Integer guestCode = null;

            Object memberObj = session.getAttribute("member");
            Object guestObj = session.getAttribute("guest");

            if (memberObj != null) {
                MemberDto member = (MemberDto) memberObj;
                memCode = member.getMemberCode();
            } else if (guestObj != null) {
                GuestDto guest = (GuestDto) guestObj;
                guestCode = guest.getGuestCode();
            } else {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 좋아요 토글 처리
            boolean isLiked = reviewService.toggleReviewLike(reviewCode, memCode, guestCode);

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("message", isLiked ? "좋아요를 추가했습니다." : "좋아요를 취소했습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "좋아요 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/detail/{reviewCode}")
    public String reviewDetail(@PathVariable("reviewCode") int reviewCode, HttpSession session, Model model) {
        try {
            // 현재 사용자 정보 조회
            Integer memCode = null;
            Integer guestCode = null;

            Object memberObj = session.getAttribute("member");
            Object guestObj = session.getAttribute("guest");

            if (memberObj != null) {
                MemberDto member = (MemberDto) memberObj;
                memCode = member.getMemberCode();
            } else if (guestObj != null) {
                GuestDto guest = (GuestDto) guestObj;
                guestCode = guest.getGuestCode();
            }

            // 리뷰 상세 정보 조회
            ReviewDetailDto reviewDetail = reviewService.getReviewDetail(reviewCode, memCode, guestCode);
            if (reviewDetail == null) {
                return "redirect:/error";
            }

            // 관련 리뷰 조회 (같은 상품의 다른 리뷰들)
            int productCode = reviewDetail.getProductDto().getProductCode();
            List<Map<String, Object>> relatedReviews = reviewService.getRelatedReviews(productCode, reviewCode, 6);

            model.addAttribute("review", reviewDetail);
            model.addAttribute("relatedReviews", relatedReviews);

            return "review/reviewDetail";

        } catch (Exception e) {
            log.error("리뷰 상세 페이지 조회 중 오류 발생 - reviewCode: {}", reviewCode, e);
            return "redirect:/error";
        }
    }

    @GetMapping("/write")
    public String writeReviewPage(@RequestParam("orderItemCode") int orderItemCode, Model model) {
        OrderItemDto orderItem = orderService.getOrderItemDetail(orderItemCode);
        if (orderItem == null) {
            return "redirect:/error";
        }
        System.out.println(orderItem);
        model.addAttribute("orderItem", orderItem);
        model.addAttribute("product", orderItem.getProductDto());
        model.addAttribute("option", orderItem.getProductOptionDto());
        model.addAttribute("image", orderItem.getProductDto().getImageDto()); // 단일 이미지

        return "review/reviewWrite";
    }

    @PostMapping("/write")
    @ResponseBody
    public ResponseEntity<?> writeReview(
            @RequestParam("orderItemCode") int orderItemCode,
            @RequestParam("rating") int rating,
            @RequestParam("reviewText") String reviewText,
            @RequestPart(value = "images[]", required = false) List<MultipartFile> imageFiles,
            HttpSession session) throws IOException {

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setOrderItemCode(orderItemCode);
        reviewDto.setContent(reviewText);
        reviewDto.setRating(rating);

        Object memberObj = session.getAttribute("member");
        Object guestObj = session.getAttribute("guest");

        if (memberObj != null) {
            MemberDto member = (MemberDto) memberObj;
            reviewDto.setMemCode(member.getMemberCode());
        } else if (guestObj != null) {
            GuestDto guest = (GuestDto) guestObj;
            reviewDto.setGuestCode(guest.getGuestCode());
        }

        reviewService.writeReview(reviewDto, imageFiles);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-eligibility")
    @ResponseBody
    public Map<String, Object> checkReviewEligibility(@RequestBody Map<String, Object> paramMap, HttpSession session) {
        MemberDto member = new MemberDto();
        GuestDto guest = new GuestDto();
        Integer orderItemCode = paramMap.get("orderItemCode") != null ?
                Integer.parseInt(paramMap.get("orderItemCode").toString()) : null;
        if(session.getAttribute("member") != null){
            member = (MemberDto) session.getAttribute("member");
        } else if(session.getAttribute("guest") != null){
            guest = (GuestDto) session.getAttribute("guest");
        }

        Map<String, Object> result = new HashMap<>();
        System.out.println(orderItemCode+" : "+ member.getMemberCode()+" : "+guest.getGuestCode());
        boolean eligible = reviewService.checkReviewEligibility(orderItemCode, member.getMemberCode(), guest.getGuestCode());
        result.put("eligible", eligible);

        return result;
    }

    /**
     * 리뷰 수정 페이지
     */
    @GetMapping("/edit/{reviewCode}")
    public String editReviewPage(@PathVariable("reviewCode") int reviewCode, HttpSession session, Model model) {
        try {
            // 현재 사용자 정보 조회
            Integer memCode = null;
            Integer guestCode = null;

            Object memberObj = session.getAttribute("member");
            Object guestObj = session.getAttribute("guest");

            if (memberObj != null) {
                MemberDto member = (MemberDto) memberObj;
                memCode = member.getMemberCode();
            } else if (guestObj != null) {
                GuestDto guest = (GuestDto) guestObj;
                guestCode = guest.getGuestCode();
            }

            // 수정 권한 확인
            if (!reviewService.canEditReview(reviewCode, memCode, guestCode)) {
                return "redirect:/review/detail/" + reviewCode + "?error=permission";
            }

            // 리뷰 상세 정보 조회
            ReviewDetailDto reviewDetail = reviewService.getReviewDetail(reviewCode, memCode, guestCode);
            if (reviewDetail == null) {
                return "redirect:/error";
            }

            model.addAttribute("review", reviewDetail);
            model.addAttribute("product", reviewDetail.getProductDto());
            model.addAttribute("option", reviewDetail.getProductOptionDto());
            model.addAttribute("productImage", reviewDetail.getProductImageDto());

            return "review/reviewEdit";

        } catch (Exception e) {
            log.error("리뷰 수정 페이지 조회 중 오류 발생 - reviewCode: {}", reviewCode, e);
            return "redirect:/error";
        }
    }

    /**
     * 리뷰 수정 처리
     */
    @PostMapping("/edit/{reviewCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable("reviewCode") int reviewCode,
            @RequestParam("rating") int rating,
            @RequestParam("reviewText") String reviewText,
            @RequestPart(value = "newImages[]", required = false) List<MultipartFile> newImages,
            @RequestParam(value = "removeImageIds", required = false) List<Integer> removeImageIds,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 현재 사용자 정보 조회
            Integer memCode = null;
            Integer guestCode = null;

            Object memberObj = session.getAttribute("member");
            Object guestObj = session.getAttribute("guest");

            if (memberObj != null) {
                MemberDto member = (MemberDto) memberObj;
                memCode = member.getMemberCode();
            } else if (guestObj != null) {
                GuestDto guest = (GuestDto) guestObj;
                guestCode = guest.getGuestCode();
            }

            // 리뷰 수정 처리
            boolean updateSuccess = reviewService.updateReview(
                    reviewCode, reviewText, rating, memCode, guestCode, newImages, removeImageIds);

            if (updateSuccess) {
                response.put("success", true);
                response.put("message", "리뷰가 성공적으로 수정되었습니다.");
                response.put("redirectUrl", "/review/detail/" + reviewCode);
            } else {
                response.put("success", false);
                response.put("message", "리뷰 수정에 실패했습니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("리뷰 수정 처리 중 오류 발생 - reviewCode: {}", reviewCode, e);
            response.put("success", false);
            response.put("message", "리뷰 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 리뷰 삭제 처리
     */
    @PostMapping("/delete/{reviewCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable("reviewCode") int reviewCode,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 현재 사용자 정보 조회
            Integer memCode = null;
            Integer guestCode = null;

            Object memberObj = session.getAttribute("member");
            Object guestObj = session.getAttribute("guest");

            if (memberObj != null) {
                MemberDto member = (MemberDto) memberObj;
                memCode = member.getMemberCode();
            } else if (guestObj != null) {
                GuestDto guest = (GuestDto) guestObj;
                guestCode = guest.getGuestCode();
            }

            // 리뷰 삭제 처리
            boolean deleteSuccess = reviewService.deleteReview(reviewCode, memCode, guestCode);

            if (deleteSuccess) {
                response.put("success", true);
                response.put("message", "리뷰가 성공적으로 삭제되었습니다.");
                response.put("redirectUrl", "/mypage/review");
            } else {
                response.put("success", false);
                response.put("message", "리뷰 삭제에 실패했습니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("리뷰 삭제 처리 중 오류 발생 - reviewCode: {}", reviewCode, e);
            response.put("success", false);
            response.put("message", "리뷰 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}