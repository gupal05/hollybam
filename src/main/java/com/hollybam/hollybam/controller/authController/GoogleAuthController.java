package com.hollybam.hollybam.controller.authController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Controller
public class GoogleAuthController {
    @PostMapping("/auth/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> tokenMap) {
        String idTokenString = tokenMap.get("idToken");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList("92346439771-d4b0tga52c1klh9f0qnrc31h51i8vfqm.apps.googleusercontent.com"))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                log.info("전체 Payload: {}", payload);

                String userId = payload.getSubject();
                String email = payload.getEmail();
                boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                // TODO: 사용자 DB 처리(회원가입/로그인 처리)

                return ResponseEntity.ok(Map.of("status", "success", "email", email, "name", name));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID token.");
            }
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token verification failed.");
        }
    }
}
