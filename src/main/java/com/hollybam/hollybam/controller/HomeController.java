package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.GuestDto;
import com.hollybam.hollybam.dto.ProductDto;
import com.hollybam.hollybam.services.GuestService;
import com.hollybam.hollybam.services.IF_SignupService;
import com.hollybam.hollybam.services.ProductService;
import com.hollybam.hollybam.services.nice.NiceCryptoTokenService;
import com.hollybam.hollybam.util.NiceCryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
    private IF_SignupService signupService;

    @GetMapping("/")
    public String introPage(HttpServletRequest request, Model model) {
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
    public ModelAndView mainPage(ModelAndView mav, HttpServletRequest request){
        List<ProductDto> proList = new ArrayList<>();
        String userAgent = request.getHeader("User-Agent");
        String deviceType = detectDevice(userAgent);
        if(deviceType.equals("pc")){
            proList = productService.selectBestProducts();
        } else {
            proList = productService.selectBestProductsForMobile();
        }
        for(int i = 0; i < proList.size(); i++){
            proList.get(i).setProductQuantity(productService.getWishCount(proList.get(i).getProductCode()));
        }
        List<ProductDto> newProList = productService.selectNewProducts();
        for(int i = 0; i < newProList.size(); i++){
            newProList.get(i).setProductQuantity(productService.getWishCount(newProList.get(i).getProductCode()));
        }
        mav.addObject("proList", proList);
        mav.addObject("newProList", newProList);
        mav.setViewName("main");
        return mav;
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
}
