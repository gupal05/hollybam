package com.hollybam.hollybam.controller.reviewController;

import com.hollybam.hollybam.dto.*;
import com.hollybam.hollybam.services.IF_OrderService;
import com.hollybam.hollybam.services.IF_ReviewService;
import com.hollybam.hollybam.util.S3Uploader;
import jakarta.servlet.http.HttpSession;
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

    @GetMapping("/detail/{reviewId}")
    public String reviewDetail(@PathVariable("reviewId") int reviewId){
        return "review/reviewDetail";
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
    public Map<String, Object> checkReviewEligibility(@RequestBody Map<String, Object> paramMap) {
        Integer orderItemCode = paramMap.get("orderItemCode") != null ?
                Integer.parseInt(paramMap.get("orderItemCode").toString()) : null;
        Integer memCode = paramMap.get("memCode") != null ?
                Integer.parseInt(paramMap.get("memCode").toString()) : null;
        Integer guestCode = paramMap.get("guestCode") != null ?
                Integer.parseInt(paramMap.get("guestCode").toString()) : null;

        Map<String, Object> result = new HashMap<>();
        boolean eligible = reviewService.checkReviewEligibility(orderItemCode, memCode, guestCode);
        result.put("eligible", eligible);

        return result;
    }
}