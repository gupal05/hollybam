package com.hollybam.hollybam.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Base64;

@Component
public class AccessLogger {

    private static final Logger accessLog = LoggerFactory.getLogger("ACCESS_LOG");

    public void logGuestAccess(String di, String action, String ip) {
        accessLog.info("Guest Access - DI: {}, Action: {}, IP: {}",
                hashDI(di), action, ip);
    }

    public void logMemberAccess(String memberId, String action, String ip) {
        accessLog.info("Member Access - ID: {}, Action: {}, IP: {}",
                memberId, action, ip);
    }

    // DI는 민감 정보이므로 해시화해서 로그 저장
    private String hashDI(String di) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(di.getBytes());
            return Base64.getEncoder().encodeToString(hash).substring(0, 8);
        } catch (Exception e) {
            return "HASH_ERROR";
        }
    }
}
