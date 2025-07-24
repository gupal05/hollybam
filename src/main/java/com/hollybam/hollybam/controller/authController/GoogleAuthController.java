package com.hollybam.hollybam.controller.authController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.IF_SignupService;
import com.hollybam.hollybam.services.LoginService;
import com.hollybam.hollybam.services.nice.NiceCryptoTokenService;
import com.hollybam.hollybam.util.NiceCryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class GoogleAuthController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private IF_SignupService signupService;
    @Autowired
    private NiceCryptoTokenService niceCryptoTokenService;

    @Value("${google.client.id}")
    private String GOOGLE_CLIENT_ID;

    @PostMapping("/auth/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> tokenMap, HttpSession session) {
        String idTokenString = tokenMap.get("idToken");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                log.info("전체 Payload: {}", payload);

                // 구글에서 받은 필요한 정보 추출
                String googleSub = payload.getSubject(); // sub -> mem_id로 사용
                String email = payload.getEmail();       // email -> mem_mail로 사용
                String name = (String) payload.get("name"); // name -> mem_name으로 사용
                boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());

                log.info("구글 사용자 정보 - Sub: {}, 이름: {}, 이메일: {}, 이메일 인증: {}",
                        googleSub, name, email, emailVerified);

                // 구글 ID를 기반으로 회원 ID 생성 (google_ prefix 추가)
                String memberId = "google_" + googleSub;

                // 기존 회원 여부 확인
                if (loginService.isGoogleMember(memberId) > 0) {
                    // 기존 회원 - 바로 로그인 처리
                    MemberDto member = loginService.getMemberInfo(memberId);
                    session.setAttribute("member", member);

                    log.info("기존 구글 회원 로그인 완료: {}", member.getMemberId());

                    return ResponseEntity.ok(Map.of(
                            "status", "existing_user",
                            "message", "로그인 성공",
                            "redirectUrl", "/loading"
                    ));
                } else {
                    // 신규 회원 - 임시 회원정보 생성 후 휴대폰 본인인증 필요
                    MemberDto tempMember = new MemberDto();
                    tempMember.setMemberId(memberId);
                    tempMember.setMemberName(name);
                    tempMember.setMemberMail(email);
                    tempMember.setMemberLoginType("google");
                    tempMember.setCreateAt(LocalDateTime.now());
                    tempMember.setUpdateAt(LocalDateTime.now());

                    // 세션에 임시 회원 정보 저장
                    session.setAttribute("temp_google_member", tempMember);

                    log.info("신규 구글 회원 - 휴대폰 인증 필요: {}", tempMember.getMemberId());

                    return ResponseEntity.ok(Map.of(
                            "status", "new_user",
                            "message", "휴대폰 본인인증이 필요합니다",
                            "redirectUrl", "/google/phone-verification"
                    ));
                }

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "error", "message", "Invalid ID token."));
            }
        } catch (GeneralSecurityException | IOException e) {
            log.error("구글 토큰 검증 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Token verification failed."));
        }
    }

    /**
     * 구글 로그인 후 휴대폰 본인인증 페이지
     */
    @GetMapping("/google/phone-verification")
    public String showGooglePhoneVerification(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 임시 회원 정보가 세션에 있는지 확인
        MemberDto tempMember = (MemberDto) session.getAttribute("temp_google_member");
        if (tempMember == null) {
            redirectAttributes.addFlashAttribute("error", "세션이 만료되었습니다. 다시 로그인해주세요.");
            return "redirect:/";
        }

        try {
            // NICE 인증 토큰 요청 (구글용 returnUrl 사용)
            Map<String, String> token = niceCryptoTokenService.requestCryptoTokenForGoogle();
            model.addAttribute("token", token);
            model.addAttribute("memberName", tempMember.getMemberName());

            return "googlePhoneVerification"; // 새로 만들 템플릿
        } catch (Exception e) {
            log.error("구글 본인인증 준비 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "본인인증 준비 중 오류가 발생했습니다.");
            return "redirect:/";
        }
    }

    /**
     * 휴대폰 본인인증 완료 후 회원가입 완료 처리
     */
    @PostMapping("/google/complete-registration")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeGoogleRegistration(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 세션에서 임시 회원 정보와 휴대폰 인증 정보 가져오기
            MemberDto tempMember = (MemberDto) session.getAttribute("temp_google_member");
            Map<String, String> phoneAuthData = (Map<String, String>) session.getAttribute("google_phone_auth_data");

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
                String type = signupService.getMemberType(di);
                type = switch (type) {
                    case "google" -> "구글";
                    case "naver" -> "네이버";
                    case "web" -> "홀리밤 사이트";
                    default -> type;
                };
                response.put("success", false);
                response.put("dupMessage", "기존에 "+type+"로 가입하신 계정이 존재합니다.");
                response.put("isDuplicate", true);

                // 세션 정리
                session.removeAttribute("temp_google_member");
                session.removeAttribute("google_phone_auth_data");

                log.info("구글 로그인 중복 가입 시도 차단: DI={}", di);
                return ResponseEntity.ok(response);
            }

            // 3. 휴대폰 인증 데이터로 최종 회원 정보 완성
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

            // 4. DB에 회원 정보 저장
            log.info("구글 회원가입 정보: {}", tempMember);
            signupService.signup(tempMember);

            // 5. 세션에 최종 회원 정보 저장 및 임시 데이터 정리
            session.setAttribute("member", tempMember);
            session.removeAttribute("temp_google_member");
            session.removeAttribute("google_phone_auth_data");

            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");

            log.info("구글 로그인 + 휴대폰 인증 회원가입 완료: {}", tempMember.getMemberId());

        } catch (Exception e) {
            log.error("구글 회원가입 완료 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "회원가입 처리 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * NICE 인증 결과를 받아서 구글 회원가입 용으로 처리하는 메서드
     */
    @PostMapping("/google/nice/result")
    public String googleNiceCallback(HttpServletRequest request, HttpSession session, Model model) {
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
                // 구글 회원가입용 휴대폰 인증 데이터 저장
                Map<String, String> phoneAuthData = new HashMap<>();
                phoneAuthData.put("di", resultMap.get("di"));
                phoneAuthData.put("birthdate", birthdate);
                phoneAuthData.put("name", name);
                phoneAuthData.put("gender", resultMap.get("gender"));
                phoneAuthData.put("phone", resultMap.get("mobileno"));

                session.setAttribute("google_phone_auth_data", phoneAuthData);

                log.info("구글 회원가입용 휴대폰 인증 완료: 이름={}, 성인여부={}", name, isAdult);
            }

            model.addAttribute("isAdult", isAdult);
            model.addAttribute("authType", "google"); // 구글 인증임을 표시

            return "authPopupCallback";

        } catch (Exception e) {
            log.error("구글 NICE 콜백 처리 중 오류 발생", e);
            model.addAttribute("isAdult", false);
            model.addAttribute("authType", "google");
            return "authPopupCallback";
        }
    }

    @GetMapping("/google/nice/result")
    public String googleNiceCallbackGet(HttpServletRequest request, HttpSession session, Model model) {
        log.info("=== 네이버 회원가입용 NICE GET 콜백 요청 받음 ===");
        return googleNiceCallback(request, session, model);
    }

    /**
     * 성인 여부 확인 메서드
     */
    private boolean isAdult(String birthdate) {
        try {
            if (birthdate == null || birthdate.length() != 8) {
                return false;
            }

            LocalDate birth = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate today = LocalDate.now();

            return !birth.isAfter(today.minusYears(19));
        } catch (Exception e) {
            log.error("생년월일 파싱 오류: {}", birthdate, e);
            return false;
        }
    }
}