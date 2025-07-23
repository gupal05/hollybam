package com.hollybam.hollybam.controller.mypageController;

import com.hollybam.hollybam.dto.*;
import com.hollybam.hollybam.services.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mypage")
public class MypageController {
    @Autowired
    MypageService mypageService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private IF_WishlistService wishlistService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private IF_OrderService orderService;
    @Autowired
    private ReviewService reviewService;

    @GetMapping("")
    public String mypage(HttpSession session, Model model) {
        try {
            // 세션에서 사용자 정보 가져오기
            MemberDto member = (MemberDto) session.getAttribute("member");
            Integer memCode = member != null ? member.getMemberCode() : null;
            GuestDto guest = (GuestDto) session.getAttribute("guest");
            List<OrderDto> orders;

            // 위시리스트 데이터 가져오기 (최신 3개만)
            List<WishlistDto> recentWishlist;
            int totalWishlistCount = 0;
            int couponCount = 0;
            if (memCode != null) {
                // 회원
                List<WishlistDto> allWishlist = wishlistService.getMemberWishlist(memCode);
                recentWishlist = allWishlist.stream().limit(3).toList();
                totalWishlistCount = wishlistService.getMemberWishlistCount(memCode);
                couponCount = couponService.selectCouponCount(memCode);
                int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
                String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);
                orders = orderService.selectOrdersByMemberForLimit(member.getMemberCode());
                for(int i = 0; i < orders.size(); i++) {
                    orders.get(i).setOrderStatus(this.getStatusText(orders.get(i).getOrderStatus()));
                    if(orders.get(i).getOrderStatus().equals("배송중")){
                        orders.get(i).setDeliveryDto(orderService.getTrackingNumber(orders.get(i).getOrderCode()));
                    }
                }
                System.out.println(orders);
                model.addAttribute("recentOrders", orders);
                model.addAttribute("paidCount", mypageService.getMemberPaidCount(member.getMemberCode()));
                model.addAttribute("shippedCount", mypageService.getMemberShippedCount(member.getMemberCode()));
                model.addAttribute("deliveredCount", mypageService.getMemberDeliveredCount(member.getMemberCode()));
                model.addAttribute("cancelCount", mypageService.getMemberCancelCount(member.getMemberCode()));
                model.addAttribute("totalPoint", totalPoint);
            } else if (guest != null) {
                // 비회원
                int guestCode = guest.getGuestCode();
                if (guestCode != 0) {
                    List<WishlistDto> allWishlist = wishlistService.getGuestWishlist(guestCode);
                    recentWishlist = allWishlist.stream().limit(3).toList();
                    totalWishlistCount = wishlistService.getGuestWishlistCount(guestCode);
                    orders = orderService.selectOrdersByGuestForLimit(guest.getGuestCode());
                    for(int i = 0; i < orders.size(); i++) {
                        orders.get(i).setOrderStatus(this.getStatusText(orders.get(i).getOrderStatus()));
                        if(orders.get(i).getOrderStatus().equals("배송중")){
                            orders.get(i).setDeliveryDto(orderService.getTrackingNumber(orders.get(i).getOrderCode()));
                        }
                    }
                    model.addAttribute("recentOrders", orders);
                    model.addAttribute("paidCount", mypageService.getGuestPaidCount(guest.getGuestCode()));
                    model.addAttribute("shippedCount", mypageService.getGuestShippedCount(guest.getGuestCode()));
                    model.addAttribute("deliveredCount", mypageService.getGuestDeliveredCount(guest.getGuestCode()));
                    model.addAttribute("cancelCount", mypageService.getGuestCancelCount(guest.getGuestCode()));
                } else {
                    recentWishlist = List.of();
                }
            } else {
                recentWishlist = List.of();
            }

            // 모델에 데이터 추가
            model.addAttribute("couponCount", couponCount);
            model.addAttribute("recentWishlist", recentWishlist);
            model.addAttribute("totalWishlistCount", totalWishlistCount);
            model.addAttribute("isLoggedIn", memCode != null);

            return "mypage/mypage";

        } catch (Exception e) {
            e.printStackTrace();
            // 에러 발생 시에도 빈 리스트로 처리
            model.addAttribute("recentWishlist", List.of());
            model.addAttribute("totalWishlistCount", 0);
            return "mypage/mypage";
        }
    }

    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) String status, Model model, HttpSession session) {
        int couponCount = 0;
        List<OrderDto> orders;

        if(session.getAttribute("member") != null) {
            MemberDto member = (MemberDto) session.getAttribute("member");
            couponCount = couponService.selectCouponCount(member.getMemberCode());
            int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
            String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);
            orders = orderService.selectOrdersByMember(member.getMemberCode());

            for(int i = 0; i < orders.size(); i++) {
                orders.get(i).setOrderStatus(this.getStatusText(orders.get(i).getOrderStatus()));
                if(orders.get(i).getOrderStatus().equals("배송중")){
                    orders.get(i).setDeliveryDto(orderService.getTrackingNumber(orders.get(i).getOrderCode()));
                }
                if(orders.get(i).getOrderStatus().equals("배송완료")){
                    for(int j=0; j<orders.get(i).getOrderItems().size(); j++){
                        if(reviewService.isWroteReview(orders.get(i).getOrderItems().get(j).getOrderItemCode()) > 0){
                            orders.get(i).getOrderItems().get(j).setReviewDto(new ReviewDto());
                            orders.get(i).getOrderItems().get(j).getReviewDto().setIsReview(true);
                            System.out.println("작성 한 주문 : "+orders.get(i).getOrderItems().get(j));
                        }else{
                            orders.get(i).getOrderItems().get(j).setReviewDto(new ReviewDto());
                            orders.get(i).getOrderItems().get(j).getReviewDto().setIsReview(false);
                            System.out.println("작성 안 한 주문 : "+orders.get(i).getOrderItems().get(j));
                        }
                    }
                }
            }

            // 상태별 필터링 추가
            if (status != null && !status.isEmpty()) {
                String filterStatus = getStatusText(status);
                orders = orders.stream()
                        .filter(order -> order.getOrderStatus().equals(filterStatus))
                        .collect(Collectors.toList());
            }

            model.addAttribute("isMember", session.getAttribute("member") != null);
            model.addAttribute("orderList", orders);
            model.addAttribute("totalPoint", totalPoint);
            model.addAttribute("couponCount", couponCount);
        } else {
            GuestDto guest = (GuestDto) session.getAttribute("guest");
            orders = orderService.selectOrdersByGuest(guest.getGuestCode());

            for(int i = 0; i < orders.size(); i++) {
                orders.get(i).setOrderStatus(this.getStatusText(orders.get(i).getOrderStatus()));
                if(orders.get(i).getOrderStatus().equals("배송중")){
                    orders.get(i).setDeliveryDto(orderService.getTrackingNumber(orders.get(i).getOrderCode()));
                }
                if(orders.get(i).getOrderStatus().equals("배송완료")){
                    for(int j=0; j<orders.get(i).getOrderItems().size(); j++){
                        if(reviewService.isWroteReview(orders.get(i).getOrderItems().get(j).getOrderItemCode()) > 0){
                            orders.get(i).getOrderItems().get(j).setReviewDto(new ReviewDto());
                            orders.get(i).getOrderItems().get(j).getReviewDto().setIsReview(true);
                            System.out.println("작성 한 주문 : "+orders.get(i).getOrderItems().get(j));
                        }else{
                            orders.get(i).getOrderItems().get(j).setReviewDto(new ReviewDto());
                            orders.get(i).getOrderItems().get(j).getReviewDto().setIsReview(false);
                            System.out.println("작성 안 한 주문 : "+orders.get(i).getOrderItems().get(j));
                        }
                    }
                }
            }

            // 비회원도 필터링 추가
            if (status != null && !status.isEmpty()) {
                String filterStatus = getStatusText(status);
                orders = orders.stream()
                        .filter(order -> order.getOrderStatus().equals(filterStatus))
                        .collect(Collectors.toList());
            }

            model.addAttribute("isGuest", session.getAttribute("guest") != null);
            model.addAttribute("orderList", orders);
        }
        return "mypage/orderList";
    }

    // 영문 상태를 한글로 매핑하는 메서드 추가
    private String getStatusText(String englishStatus) {
        switch (englishStatus) {
            case "PENDING": return "주문 보류";
            case "PAID", "PREPARING": return "결제완료";
            case "SHIPPED": return "배송중";
            case "DELIVERED": return "배송완료";
            case "CANCELLED": return "취소됨";
            case "REFUNDED": return "반품";
            default: return "기타";
        }
    }

    @GetMapping("/profile/edit")
    public ModelAndView profileEdit(ModelAndView mav, HttpSession session){
        int couponCount = 0;
        MemberDto member = (MemberDto)session.getAttribute("member");
        String phone = member.getMemberPhone().replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        couponCount = couponService.selectCouponCount(member.getMemberCode());
        int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
        String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);
        mav.addObject("totalPoint", totalPoint);
        mav.addObject("couponCount", couponCount);
        mav.addObject("phone", phone);
        mav.setViewName("mypage/change-info");
        return mav;
    }

    @PostMapping("/member/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> memberUpdate(@ModelAttribute MemberDto memberDto, HttpSession session){
        System.out.println(memberDto);
        Map<String, Object> map = new HashMap<>();
        try {
            MemberDto member = (MemberDto)session.getAttribute("member");
            member.setMemberPw(passwordEncoder.encode(memberDto.getMemberPw()));
            mypageService.updateMember(member);
            map.put("success", true);
            map.put("message", "비밀번호가 성공적으로 수정되었습니다.");
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            map.put("success", false);
            map.put("message", "비밀번호 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
        }
    }

    @GetMapping("/coupons")
    public String couponPage(HttpSession session, Model model) {
        int couponPossibleCount = 0;
        int couponCount = 0;
        int useCouponCount = 0;
        int expirationCouponCount = 0;
        List<Map<String, Object>> couponList = new ArrayList<>();

        if(session.getAttribute("member") != null) {
            MemberDto member = (MemberDto) session.getAttribute("member");
            int memCode = member.getMemberCode();

            // 쿠폰 통계 조회
            couponCount = couponService.selectTotalCouponCount(memCode);
            couponPossibleCount = couponService.selectCouponCount(memCode);
            useCouponCount = couponService.selectUsedCouponCount(memCode);
            expirationCouponCount = couponService.expirationCouponCount(memCode);

            // 전체 쿠폰 목록 조회
            couponList = couponService.selectMemberCouponList(memCode, "all");

            // 디버깅: 실제 Map 키 확인
            if (!couponList.isEmpty()) {
                System.out.println("=== 쿠폰 데이터 키 확인 ===");
                Map<String, Object> firstCoupon = couponList.get(0);
                System.out.println("Available keys: " + firstCoupon.keySet());
                System.out.println("Sample data: " + firstCoupon);
            }

            int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
            String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);

            model.addAttribute("totalPoint", totalPoint);
            model.addAttribute("couponCount", couponCount);
            model.addAttribute("couponPossibleCount", couponPossibleCount);
            model.addAttribute("useCouponCount", useCouponCount);
            model.addAttribute("expirationCouponCount", expirationCouponCount);
            model.addAttribute("couponList", couponList);

            System.out.println("Total coupons: " + couponCount);
            System.out.println("Used coupons: " + useCouponCount);
            System.out.println("Coupon list size: " + couponList.size());
            for (int i=0; i<couponList.size(); i++) {
                System.out.println(couponList.get(i));
            }
        }

        return "mypage/coupons";
    }

    /**
     * AJAX로 필터별 쿠폰 목록 조회
     */
    @GetMapping("/coupons/filter")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getCouponsByFilter(
            HttpSession session,
            @RequestParam(value = "status", defaultValue = "all") String status) {

        if(session.getAttribute("member") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MemberDto member = (MemberDto) session.getAttribute("member");
        List<Map<String, Object>> couponList = couponService.selectMemberCouponList(member.getMemberCode(), status);

        return ResponseEntity.ok(couponList);
    }

//    /**
//     * 쿠폰 사용 처리 (장바구니에서 사용할 때)
//     */
//    @PostMapping("/coupons/use")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> useCoupon(
//            HttpSession session,
//            @RequestParam("couponMemberCode") int couponMemberCode,
//            @RequestParam(value = "orderCode", required = false) Integer orderCode) {
//
//        Map<String, Object> result = new HashMap<>();
//
//        if(session.getAttribute("member") == null) {
//            result.put("success", false);
//            result.put("message", "로그인이 필요합니다.");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
//        }
//
//        try {
//            // 주문 코드가 없으면 임시로 0 설정 (장바구니에서 선택만 하는 경우)
//            if(orderCode == null) {
//                orderCode = 0;
//            }
//
//            boolean success = couponService.useCoupon(couponMemberCode, orderCode);
//
//            if(success) {
//                result.put("success", true);
//                result.put("message", "쿠폰이 선택되었습니다.");
//            } else {
//                result.put("success", false);
//                result.put("message", "쿠폰 사용에 실패했습니다.");
//            }
//
//        } catch (Exception e) {
//            result.put("success", false);
//            result.put("message", "오류가 발생했습니다: " + e.getMessage());
//        }
//
//        return ResponseEntity.ok(result);
//    }

    /**
     * 쿠폰 등록 API
     */
    @PostMapping("/coupons/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerCoupon(
            @RequestParam("couponNumber") String couponNumber,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 로그인 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }
            System.out.println(couponNumber);
            // 쿠폰 등록 처리
            Map<String, Object> result = couponService.registerCoupon(couponNumber, member.getMemberCode());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "쿠폰 등록 중 오류가 발생했습니다.");
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/points")
    public String pointsPage(HttpSession session, Model model,
                             @RequestParam(defaultValue = "1") int page) {
        MemberDto member = (MemberDto) session.getAttribute("member");
        int couponPossibleCount = couponService.selectCouponCount(member.getMemberCode());
        int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
        int addPoints = mypageService.selectMemberAddPoint(member.getMemberCode());
        int usePoints = mypageService.selectMemberUsePoint(member.getMemberCode());

        // 페이지네이션 설정
        int pageSize = 10;
        List<PointDto> pointHistory = mypageService.selectPointHistory(member.getMemberCode(), page, pageSize);
        int totalCount = mypageService.selectPointHistoryCount(member.getMemberCode());
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);
        String addPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(addPoints);
        String usePoint = NumberFormat.getNumberInstance(Locale.KOREA).format(usePoints);

        model.addAttribute("couponCount", couponPossibleCount);
        model.addAttribute("totalPoint", totalPoint);
        model.addAttribute("addPoint", addPoint);
        model.addAttribute("usePoint", usePoint);
        model.addAttribute("pointHistory", pointHistory);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "mypage/points";
    }



    @GetMapping("/order/detail/{orderId}")
    @ResponseBody
    public Map<String, Object> getOrderDetail(@PathVariable("orderId") String orderId, @RequestParam int orderCode) {
        Map<String, Object> result = new HashMap<>();
        List<OrderListDto> orderListDto = mypageService.selectOrderDetailByOrderCode(orderCode);
        for(int i=0; i<orderListDto.size(); i++) {
            if(orderListDto.get(i).getOrderStatus().equals("PREPARING")) {
                orderListDto.get(i).setOrderStatus("PAID");
            }
        }
        if(orderListDto != null) {
            result.put("orderListDto", orderListDto);
            result.put("message", true);
        }else{
            result.put("message", false);
        }
        return result;
    }

    @GetMapping("/review")
    public String reviewPage(HttpSession session, Model model) {
        Map<String,Object> map = new HashMap<>();
        String totalPoint = "";
        int couponCount = 0;
        if(session.getAttribute("member") != null){
            MemberDto member = (MemberDto)session.getAttribute("member");
            map = reviewService.selectMemberReviewStats(member.getMemberCode());
            couponCount = couponService.selectCouponCount(member.getMemberCode());
            int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
            totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);
        }else{
            GuestDto guest = (GuestDto)session.getAttribute("guest");
            map = reviewService.selectGuestReviewStats(guest.getGuestCode());
        }
        System.out.println(map.toString());
        model.addAttribute("couponCount", couponCount);
        model.addAttribute("totalPoint", totalPoint);
        model.addAttribute("totalReviews",map.get("totalReviews"));
        model.addAttribute("photoReviews",map.get("photoReviews"));
        model.addAttribute("textReviews",map.get("textReviews"));
        model.addAttribute("avgRating",map.get("avgRating"));
        return "mypage/review";
    }

    /**
     * 마이페이지 리뷰 AJAX 조회 (페이지네이션 + 필터링)
     */
    @GetMapping("/review/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMyReviews(
            @RequestParam(value = "type", defaultValue = "photo") String type,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
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
            } else {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<Map<String, Object>> reviewList = new ArrayList<>();

            // 리뷰 타입에 따라 데이터 조회
            if ("photo".equals(type)) {
                reviewList = reviewService.getMyPhotoReviews(sort, page, size, rating, memCode, guestCode);
            } else if ("text".equals(type)) {
                reviewList = reviewService.getMyTextReviews(sort, page, size, rating, memCode, guestCode);
            }

            // 리뷰 카운트 정보 (필터링 적용)
            Map<String, Object> reviewCount = reviewService.getMyReviewCount(rating, memCode, guestCode);

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
     * 리뷰 삭제 API
     */
    @DeleteMapping("/review/{reviewCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMyReview(
            @PathVariable int reviewCode,
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

            // 본인 리뷰인지 확인 및 삭제 처리
            boolean deleted = reviewService.deleteMyReview(reviewCode, memCode, guestCode);

            if (deleted) {
                response.put("success", true);
                response.put("message", "리뷰가 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "삭제할 수 없는 리뷰입니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "리뷰 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}