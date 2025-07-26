package com.hollybam.hollybam.util;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class DataEncrypt {
    /**
     * 주어진 문자열(strData)을 SHA‑256으로 해시한 뒤 hex 문자열로 반환합니다.
     */
    public String encrypt(String strData) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(strData.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return new String(Hex.encodeHex(digest));
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 암호화 실패", e);
        }
    }
}
