package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.BestReviewDto;
import com.hollybam.hollybam.dto.GuestDto;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.ProductDto;
import com.hollybam.hollybam.services.*;
import com.hollybam.hollybam.services.admin.AdminBannerServiceImpl;
import com.hollybam.hollybam.services.admin.AdminPopupService;
import com.hollybam.hollybam.services.admin.AdminSpecialSaleService;
import com.hollybam.hollybam.services.nice.NiceCryptoTokenService;
import com.hollybam.hollybam.util.NiceCryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class HomeController {
    @Autowired
    private ProductService productService;
    @Autowired
    private HttpSession session;
    @Autowired
    private NiceCryptoTokenService niceCryptoTokenService;
    @Autowired
    private GuestService guestService;
    @Autowired
    private SignupService signupService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private AdminBannerServiceImpl adminBannerService;
    @Autowired
    private AdminPopupService adminPopupService;
    @Autowired
    private AdminSpecialSaleService adminSpecialSaleService;

    @GetMapping("/")
    public String introPage(HttpServletRequest request, Model model) {
        if(session.getAttribute("member") != null){
            session.removeAttribute("member");
        } else if(session.getAttribute("guest") != null){
            session.removeAttribute("guest");
        }
        try {
            log.info("=== NICE 암호화 토큰 요청 시작 ===");
            Map<String, String> result = niceCryptoTokenService.requestCryptoToken();
            log.info("토큰 발급 성공: {}", result);
            model.addAttribute("token", result);
            return "intro";
        } catch (Exception e) {
            log.error("Crypto Token 발급 실패", e);
            model.addAttribute("error", e.getMessage());
            return "intro";
        }
    }

    @GetMapping("/intro")
    public RedirectView introPermanentRedirect() {
        RedirectView rv = new RedirectView("/");
        rv.setStatusCode(HttpStatus.MOVED_PERMANENTLY); // 301
        rv.setExposeModelAttributes(false);
        return rv;
    }

    // 본인인증 return url
    @PostMapping("/nice/result")
    public String niceCallback(HttpServletRequest request, HttpSession session, Model model) {
        GuestDto guest = new GuestDto();
        try {
            String encData = request.getParameter("enc_data");

            if (encData == null || encData.trim().isEmpty()) {
                model.addAttribute("isAdult", false);
                return "authPopupCallback";
            }

            String tokenVal = (String) session.getAttribute("token_val");
            String reqDtim = (String) session.getAttribute("req_dtim");
            String reqNo = (String) session.getAttribute("req_no");

            if (tokenVal == null || reqDtim == null || reqNo == null) {
                model.addAttribute("isAdult", false);
                return "authPopupCallback";
            }

            Map<String, String> resultMap = NiceCryptoUtil.decryptEncodeData(encData, reqDtim, reqNo, tokenVal);

            session.removeAttribute("token_val");
            session.removeAttribute("req_dtim");
            session.removeAttribute("req_no");

            String birthdate = resultMap.get("birthdate");
            boolean isAdult = isAdult(birthdate);
            String name = resultMap.get("utf8_name");
            String di = resultMap.get("di");

            try {
                name = java.net.URLDecoder.decode(name, "UTF-8");
            } catch (Exception e) {
                log.warn("이름 디코딩 실패", e);
            }

            log.info("비회원 NICE 인증 완료: 성인여부={}, DI={}", isAdult, di);

            if (isAdult) {
                // ✅ 1. 먼저 member 테이블에 해당 DI로 가입한 이력이 있는지 확인
                if (signupService.isRecodeSignup(di) > 0) {
                    log.info("기존 회원 가입 이력 발견 - DI: {}", di);
                    model.addAttribute("isAdult", true);
                    model.addAttribute("isDuplicateMember", true); // 중복 회원 플래그
                    return "authPopupCallback";
                }

                // ✅ 2. member 테이블에 없으면 기존 guest 로직 수행
                if(guestService.isGuest(di) > 0) {
                    session.setAttribute("guest", guestService.getGuestByDi(di));
                } else {
                    guest.setGuestDi(di);
                    guest.setGuestName(name);
                    guest.setGuestBirth(LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("yyyyMMdd")));
                    guest.setGuestGender(resultMap.get("gender").equals("1") ? "남자" : "여자");
                    guest.setGuestPhone(resultMap.get("mobileno"));
                    guestService.insertGuest(guest);
                    session.setAttribute("guest", guestService.getGuestByDi(di));
                }
            }

            model.addAttribute("isAdult", isAdult);
            return "authPopupCallback";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("isAdult", false);
            return "authPopupCallback";
        }
    }

    // GET으로도 콜백을 받을 수 있도록 추가
    @GetMapping("/nice/result")
    public String niceCallbackGet(HttpServletRequest request, HttpSession session, Model model) {
        log.info("=== NICE GET 콜백 요청 받음 ===");
        return niceCallback(request, session, model);
    }


    @GetMapping("/main")
    public ModelAndView mainPage(ModelAndView mav, HttpServletRequest request, HttpSession session){
        List<ProductDto> proList = new ArrayList<>();
        String userAgent = request.getHeader("User-Agent");
        String deviceType = detectDevice(userAgent);
        List<Map<String,Object>> bannerList = adminBannerService.getBannerList();
        if(deviceType.equals("pc")){
            proList = productService.selectBestProducts();
        } else {
            proList = productService.selectBestProductsForMobile();
        }
        for(int i = 0; i < proList.size(); i++){
            proList.get(i).setWishCount(productService.getWishCount(proList.get(i).getProductCode()));
            if(productService.isSpecialSale(proList.get(i).getProductCode()) > 0){
                proList.get(i).setSale(true);
                proList.get(i).setSalePrice(productService.getProductDetailSalePrice(proList.get(i).getProductCode()));

                int originalPrice = proList.get(i).getPriceDtoList().get(0).getPriceOriginal();
                int salePrice = proList.get(i).getSalePrice();

                int discountRate = 0;
                if(originalPrice > 0){
                    discountRate = (int) Math.round(((originalPrice - salePrice) / (double) originalPrice) * 100);
                }
                proList.get(i).setSpecialDiscountRate(discountRate);
            }
            proList.get(i).getPriceDtoList().get(0).setPercentage((proList.get(i).getPriceDtoList().get(0).getPriceOriginal() - proList.get(i).getPriceDtoList().get(0).getPriceSelling()) * 100 / proList.get(i).getPriceDtoList().get(0).getPriceOriginal());
        }
        List<ProductDto> newProList = productService.selectNewProducts();
        for(int i = 0; i < newProList.size(); i++){
            newProList.get(i).setWishCount(productService.getWishCount(newProList.get(i).getProductCode()));
            newProList.get(i).getPriceDtoList().get(0).setPercentage((newProList.get(i).getPriceDtoList().get(0).getPriceOriginal() - newProList.get(i).getPriceDtoList().get(0).getPriceSelling()) * 100 / newProList.get(i).getPriceDtoList().get(0).getPriceOriginal());
        }

        // ⭐ 기존 코드를 최소 수정: 베스트 리뷰 조회 시 사용자별 좋아요 상태 포함
        List<BestReviewDto> bestReview = getBestReviewsWithUserLikeStatus(session);

        for(int i = 0; i < bestReview.size(); i++){
            bestReview.get(i).setWriterAge(this.getAgeGroup(bestReview.get(i).getWriterBirth()));
            bestReview.get(i).setWriterName(bestReview.get(i).getWriterName().charAt(0)+"**");
        }

        if(adminPopupService.getPopupCount() > 0){
            List<Map<String, Object>> popupList = adminPopupService.getPopupList();
            mav.addObject("popup", popupList);
        }

        mav.addObject("banner", bannerList);
        mav.addObject("bestReviews", bestReview);
        mav.addObject("proList", proList);
        mav.addObject("newProList", newProList);
        mav.setViewName("main");
        return mav;
    }

    @GetMapping("/special/sale")
    public ModelAndView specialSalePage(ModelAndView mav, HttpServletRequest request, HttpSession session){
        // 현재 날짜 정보
        LocalDate now = LocalDate.now();


        // 파라미터가 없으면 현재 날짜 사용
        int targetYear = now.getYear();
        int targetMonth = now.getMonthValue();

        // 날짜 문자열 생성 (YYYY-MM-01 형식)
        String date = String.format("%d-%02d-01", targetYear, targetMonth);

        System.out.println("조회할 날짜: " + date);

        // 특가 상품 리스트 조회
        List<Map<String, Object>> saleList = adminSpecialSaleService.selectSpecialSaleList(date);
        System.out.println("saleList : "+saleList);
        // 템플릿과 매칭되는 이름으로 전달
        mav.addObject("saleProducts", saleList);
        mav.addObject("totalCount", saleList.size()); // 총 상품 개수
        mav.setViewName("salePage");
        return mav;
    }

    /**
     * ⭐ 새로 추가: 베스트 리뷰 조회 (사용자별 좋아요 상태 포함) - 기존 코드 보존
     */
    private List<BestReviewDto> getBestReviewsWithUserLikeStatus(HttpSession session) {
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

            // 베스트 리뷰 조회 (사용자별 좋아요 상태 포함)
            return reviewService.selectBestReviewsWithLikeStatus(memCode, guestCode);

        } catch (Exception e) {
            // 오류 발생 시 기존 방식으로 폴백 (기존 코드 보존)
            return reviewService.selectBestReviews();
        }
    }

    @GetMapping("/loading")
    public String loadingPage(){ return "loading"; }

    @GetMapping("/real")
    public String realPage(){ return "real"; }


    private String detectDevice(String userAgent) {
        userAgent = userAgent.toLowerCase();

        // 모바일
        if (userAgent.contains("mobi")) {
            // 태블릿은 대부분 "tablet" 또는 "ipad" 포함
            if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
                return "tablet";
            }
            return "mobile";
        }

        // PC
        return "pc";
    }

    private boolean isAdult(String birthdate) {
        try {
            LocalDate birth = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return Period.between(birth, LocalDate.now()).getYears() >= 19;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/niceRedirect")
    public String niceRedirect(
            @RequestParam String token_version_id,
            @RequestParam String enc_data,
            @RequestParam String integrity_value,
            Model model) {

        model.addAttribute("token_version_id", token_version_id);
        model.addAttribute("enc_data", enc_data);
        model.addAttribute("integrity_value", integrity_value);

        return "niceRedirect"; // 위 HTML 템플릿
    }

    @GetMapping("/companyInfo")
    public String companyInfo() {
        return "companyInfo";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/guide")
    public String guide() {
        return "guide";
    }

    @GetMapping("/kiki")
    public String kiki() {
        session.setAttribute("guest", guestService.getGuestByDi("MC0GCCqGSIb3DQIJAyEACKfBOHYuC7XzJeid99M29aD87Fi8pI9WFEOLJw0IpnI="));
        return "loading";
    }


    public String getAgeGroup(LocalDate birthDate) {
        if (birthDate == null) return "정보 없음";

        int birthYear = birthDate.getYear();
        int thisYear = LocalDate.now().getYear();

        int age = thisYear - birthYear + 1; // 한국식 나이 계산

        int ageGroup = (age / 10) * 10; // 20, 30, 40...
        return ageGroup + "대";
    }

    @GetMapping("/brand")
    public String brand() {
        return "404";
    }

}
