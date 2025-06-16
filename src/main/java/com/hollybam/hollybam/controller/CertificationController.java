package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.CertificationDto;
import com.hollybam.hollybam.services.CertificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// 본인 인증을 위한 controller
@RestController
public class CertificationController {

    private final CertificationService certificationService;

    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @PostMapping("/verify-cert")
    public ResponseEntity<?> verifyCert(@RequestBody Map<String, String> body, HttpSession session) {
        String impUid = body.get("imp_uid");

        if (impUid == null || impUid.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "imp_uid 누락"));
        }

        try {
            CertificationDto result = certificationService.verifyAdultByImpUid(impUid);
            if (result.isAdult()) {
                // 세션 UUID 발급 또는 기존 세션 유지
                String guestUuid = Optional.ofNullable((String) session.getAttribute("guest_uuid"))
                        .orElse(UUID.randomUUID().toString());

                session.setAttribute("guest_uuid", guestUuid);

                // guest_user DB 저장
                certificationService.saveGuestUser(guestUuid, result);

                return ResponseEntity.ok(Map.of(
                        "adult", true,
                        "message", "성인 인증 완료"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "adult", false,
                        "message", "19세 미만은 접근할 수 없습니다."
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류: " + e.getMessage()));
        }
    }


}


