package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.CertificationDto;
import com.hollybam.hollybam.services.CertificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 본인 인증을 위한 controller
@RestController
public class CertificationController {

    private final CertificationService certificationService;

    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @PostMapping("/verify-cert")
    public ResponseEntity<?> verifyCert(@RequestBody Map<String, String> body) {
        String impUid = body.get("imp_uid");

        if (impUid == null || impUid.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "imp_uid 누락"));
        }

        try {
            CertificationDto result = certificationService.verifyAdultByImpUid(impUid);
            System.out.println("인증 결과: " + result); // 여기서 사용자 정보 출력

            return ResponseEntity.ok(Map.of(
                    "adult", result.isAdult(),
                    "message", result.isAdult() ? "성인 인증 완료" : "성인 인증 실패"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류: " + e.getMessage()));
        }
    }

}


