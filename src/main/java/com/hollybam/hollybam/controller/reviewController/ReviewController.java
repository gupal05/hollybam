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

    @GetMapping("")
    public String review(){
        return "review/reviewList";
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
        Integer orderItemCode = paramMap.get("orderItemCode") != null ? Integer.parseInt(paramMap.get("orderItemCode").toString()) : null;
        Integer memberCode = paramMap.get("memberCode") != null ? Integer.parseInt(paramMap.get("memberCode").toString()) : null;
        Integer guestCode = paramMap.get("guestCode") != null ? Integer.parseInt(paramMap.get("guestCode").toString()) : null;

        Map<String, Object> result = new HashMap<>();
        boolean eligible = reviewService.checkReviewEligibility(orderItemCode, memberCode, guestCode);

        result.put("eligible", eligible);
        if (!eligible) {
            result.put("message", "이미 리뷰를 작성하셨거나 조건을 만족하지 않습니다.");
        }
        return result;
    }

}
