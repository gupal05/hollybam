package com.hollybam.hollybam.controller.authController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.LoginService;
import com.hollybam.hollybam.services.SignupService;
import com.hollybam.hollybam.services.nice.NiceCryptoTokenService;
import com.hollybam.hollybam.util.NiceCryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NaverLoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private SignupService signupService;

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.redirect.uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NiceCryptoTokenService niceCryptoTokenService;
    //private final MemberService memberService; // 회원 서비스 추가

    @GetMapping("/auth/login/naver")
    public String redirectToNaverLogin() {
        String state = "RANDOM_STATE"; // TODO: CSRF 방지용 임의값 생성
        String naverLoginUrl = "https://nid.naver.com/oauth2.0/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;
        return "redirect:" + naverLoginUrl;
    }

    @GetMapping("/login/naver/callback")
    public String handleNaverCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // 1. 액세스 토큰 요청
            String tokenUrl = "https://nid.naver.com/oauth2.0/token" +
                    "?grant_type=authorization_code" +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&code=" + code +
                    "&state=" + state;

            RestTemplate restTemplate = new RestTemplate();
            String tokenResponse = restTemplate.getForObject(tokenUrl, String.class);
            JsonNode tokenJson = objectMapper.readTree(tokenResponse);
            String accessToken = tokenJson.get("access_token").asText();

            // 2. 사용자 정보 요청
            org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
            httpHeaders.set("Authorization", "Bearer " + accessToken);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(httpHeaders);

            String profileUrl = "https://openapi.naver.com/v1/nid/me";
            org.springframework.http.ResponseEntity<String> profileResponse = restTemplate
                    .exchange(profileUrl, org.springframework.http.HttpMethod.GET, entity, String.class);

            JsonNode profileJson = objectMapper.readTree(profileResponse.getBody());
            JsonNode responseJson = profileJson.get("response");

            // 3. 사용자 정보 파싱 (임시 저장)
            String id = responseJson.get("id").asText();
            String name = responseJson.has("name") ? responseJson.get("name").asText() : "";
            String email = responseJson.has("email") ? responseJson.get("email").asText() : "";
            String birthYear = responseJson.has("birthyear") ? responseJson.get("birthyear").asText() : null;
            String birthday = responseJson.has("birthday") ? responseJson.get("birthday").asText() : null;
            String gender = responseJson.has("gender") ? responseJson.get("gender").asText() : null;
            String phone = responseJson.has("mobile") ? responseJson.get("mobile").asText() : null;

            // 4. 임시 MemberDto 생성 (DB 저장 전)
            MemberDto tempMember = new MemberDto();
            tempMember.setMemberId("naver_" + id); // 네이버 계정임을 구분
            tempMember.setMemberName(name);
            tempMember.setMemberMail(email);
            tempMember.setMemberGender(gender);
            tempMember.setMemberPhone(phone);
            tempMember.setCreateAt(LocalDateTime.now());
            tempMember.setUpdateAt(LocalDateTime.now());

            // 네이버에서 생년월일 정보가 있으면 임시로 설정
            if (birthYear != null && birthday != null) {
                String[] birthParts = birthday.split("-");
                int year = Integer.parseInt(birthYear);
                int month = Integer.parseInt(birthParts[0]);
                int day = Integer.parseInt(birthParts[1]);

                LocalDate birthDate = LocalDate.of(year, month, day);
                tempMember.setMemberBirth(birthDate);

                // 네이버 정보로 성인 여부 판단 (만 19세 이상)
                LocalDate today = LocalDate.now();
                boolean isAdult = !birthDate.isAfter(today.minusYears(19));
                tempMember.setAdultVerified(isAdult);
            }
            if(loginService.isNaverMember(tempMember.getMemberId()) > 0){
                MemberDto member = loginService.getMemberInfo(tempMember.getMemberId());
                session.setAttribute("member", member);
                return  "redirect:/loading";
            } else {
                // 5. 세션에 임시 회원 정보 저장 (휴대폰 인증 완료 후 실제 DB 저장 예정)
                tempMember.setMemberLoginType("naver");
                session.setAttribute("temp_naver_member", tempMember);
                // 6. 휴대폰 본인인증 페이지로 리다이렉트
                return "redirect:/naver/phone-verification";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "네이버 로그인 중 오류가 발생했습니다.");
            return "redirect:/";
        }
    }

    /**
     * 네이버 로그인 후 휴대폰 본인인증 페이지
     */
    @GetMapping("/naver/phone-verification")
    public String showPhoneVerification(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 임시 회원 정보가 세션에 있는지 확인
        MemberDto tempMember = (MemberDto) session.getAttribute("temp_naver_member");
        if (tempMember == null) {
            redirectAttributes.addFlashAttribute("error", "세션이 만료되었습니다. 다시 로그인해주세요.");
            return "redirect:/";
        }

        try {
            // NICE 인증 토큰 요청 (네이버용 returnUrl 사용)
            Map<String, String> token = niceCryptoTokenService.requestCryptoTokenForNaver();
            model.addAttribute("token", token);
            model.addAttribute("memberName", tempMember.getMemberName());

            return "naverPhoneVerification"; // 새로 만든 템플릿
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "본인인증 준비 중 오류가 발생했습니다.");
            return "redirect:/";
        }
    }

    /**
     * 휴대폰 본인인증 완료 후 회원가입 완료 처리
     */
    @PostMapping("/naver/complete-registration")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeRegistration(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 세션에서 임시 회원 정보와 휴대폰 인증 정보 가져오기
            MemberDto tempMember = (MemberDto) session.getAttribute("temp_naver_member");
            Map<String, String> phoneAuthData = (Map<String, String>) session.getAttribute("naver_phone_auth_data");

            if (tempMember == null) {
                response.put("success", false);
                response.put("message", "세션이 만료되었습니다.");
                return ResponseEntity.ok(response);
            }

            if (phoneAuthData == null) {
                response.put("success", false);
                response.put("message", "휴대폰 인증 정보가 없습니다.");
                return ResponseEntity.ok(response);
            }

            // 2. DI로 기존 가입 여부 확인
            String di = phoneAuthData.get("di");
            if (signupService.isRecodeSignup(di) > 0) {
                // 기존 회원 존재
                response.put("success", false);
                response.put("message", "기존에 가입하신 계정이 존재합니다.");
                response.put("isDuplicate", true); // 중복 가입 플래그 추가

                // 세션 정리
                session.removeAttribute("temp_naver_member");
                session.removeAttribute("naver_phone_auth_data");

                log.info("네이버 로그인 중복 가입 시도 차단: DI={}", di);
                return ResponseEntity.ok(response);
            }

            // 2. 휴대폰 인증 데이터로 최종 회원 정보 완성
            String phoneAuthBirthdate = phoneAuthData.get("birthdate");
            String phoneAuthName = phoneAuthData.get("name");
            String phoneAuthGender = phoneAuthData.get("gender");
            String phoneAuthPhone = phoneAuthData.get("phone");

            // 휴대폰 인증 정보로 업데이트
            if (phoneAuthBirthdate != null) {
                LocalDate birthDate = LocalDate.parse(phoneAuthBirthdate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                tempMember.setMemberBirth(birthDate);

                // 휴대폰 인증 기준으로 성인 여부 재확인
                LocalDate today = LocalDate.now();
                boolean isAdult = !birthDate.isAfter(today.minusYears(19));
                tempMember.setAdultVerified(isAdult);
                tempMember.setAdultVerifiedAt(LocalDateTime.now());
            }

            if (phoneAuthName != null) {
                tempMember.setMemberName(phoneAuthName); // 휴대폰 인증 이름으로 업데이트
            }

            if (phoneAuthGender != null) {
                tempMember.setMemberGender(phoneAuthGender.equals("1") ? "남자" : "여자");
            }

            if (phoneAuthPhone != null) {
                tempMember.setMemberPhone(phoneAuthPhone);
            }

            // DI 정보 추가
            if (di != null) {
                tempMember.setDi(di);
            }

            // 3. DB에 회원 정보 저장
            System.out.println("di : "+ phoneAuthData.get("di"));
            System.out.println("템프 : "+tempMember);
            signupService.signup(tempMember);

            // 4. 세션에 최종 회원 정보 저장 및 임시 데이터 정리
            session.setAttribute("member", tempMember);
            session.removeAttribute("temp_naver_member");
            session.removeAttribute("naver_phone_auth_data");

            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");

            log.info("네이버 로그인 + 휴대폰 인증 회원가입 완료: {}", tempMember.getMemberId());

        } catch (Exception e) {
            log.error("네이버 회원가입 완료 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "회원가입 처리 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * NICE 인증 결과를 받아서 네이버 회원가입 용으로 처리하는 메서드
     */
    @PostMapping("/naver/nice/result")
    public String naverNiceCallback(HttpServletRequest request, HttpSession session, Model model) {
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

            // 토큰 정보 정리
            session.removeAttribute("token_val");
            session.removeAttribute("req_dtim");
            session.removeAttribute("req_no");

            String birthdate = resultMap.get("birthdate");
            boolean isAdult = isAdult(birthdate);
            String name = resultMap.get("utf8_name");
            try {
                name = java.net.URLDecoder.decode(name, "UTF-8");
            } catch (Exception e) {
                log.warn("이름 디코딩 실패", e);
            }

            if (isAdult) {
                // 네이버 회원가입용 휴대폰 인증 데이터 저장
                Map<String, String> phoneAuthData = new HashMap<>();
                phoneAuthData.put("di", resultMap.get("di"));
                phoneAuthData.put("birthdate", birthdate);
                phoneAuthData.put("name", name);
                phoneAuthData.put("gender", resultMap.get("gender"));
                phoneAuthData.put("phone", resultMap.get("mobileno"));

                session.setAttribute("naver_phone_auth_data", phoneAuthData);

                log.info("네이버 회원가입용 휴대폰 인증 완료: 이름={}, 성인여부={}", name, isAdult);
            }

            model.addAttribute("isAdult", isAdult);
            return "authPopupCallback";

        } catch (Exception e) {
            log.error("네이버 회원가입용 NICE 인증 처리 중 오류 발생", e);
            model.addAttribute("isAdult", false);
            return "authPopupCallback";
        }
    }

    @GetMapping("/naver/nice/result")
    public String naverNiceCallbackGet(HttpServletRequest request, HttpSession session, Model model) {
        log.info("=== 네이버 회원가입용 NICE GET 콜백 요청 받음 ===");
        return naverNiceCallback(request, session, model);
    }

    /**
     * 생년월일로 성인 여부 판단 (만 19세 이상)
     */
    private boolean isAdult(String birthdate) {
        if (birthdate == null || birthdate.length() != 8) {
            return false;
        }

        try {
            LocalDate birthDate = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate today = LocalDate.now();
            return !birthDate.isAfter(today.minusYears(19));
        } catch (Exception e) {
            log.error("생년월일 파싱 오류: {}", birthdate, e);
            return false;
        }
    }
}