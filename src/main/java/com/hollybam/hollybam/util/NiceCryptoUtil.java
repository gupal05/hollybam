package com.hollybam.hollybam.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class NiceCryptoUtil {

    public static String encryptSHA256(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(text.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    public static String encryptAES(String plainText, String key, String iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String hmac256(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmac = mac.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hmac);
    }

    // ✅ 복호화 메서드 추가
    public static String decryptAES(String encData, String key, String iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decodedBytes = Base64.getDecoder().decode(encData);
        byte[] decrypted = cipher.doFinal(decodedBytes);

        return new String(decrypted, "UTF-8");
    }

    // ✅ 복호화 후 JSON → Map 변환 메서드 추가
    public static Map<String, String> decryptEncodeData(String encData, String reqDtim, String reqNo, String tokenVal) throws Exception {
        // hash 값 생성 → key, iv, hmac_key 도출
        String resultForHash = reqDtim.trim() + reqNo.trim() + tokenVal.trim();
        String hash = encryptSHA256(resultForHash);

        String key = hash.substring(0, 16);
        String iv = hash.substring(hash.length() - 16);

        String decryptedJson = decryptAES(encData, key, iv);

        // JSON을 Map으로 변환
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(decryptedJson, HashMap.class);
    }
}
