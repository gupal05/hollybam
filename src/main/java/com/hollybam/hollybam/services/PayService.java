package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.PaysterProperties;
import com.hollybam.hollybam.util.DataEncrypt;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Service
public class PayService {
    private final PaysterProperties prop;
    private final DataEncrypt encryptor;

    public PayService(PaysterProperties prop, DataEncrypt encryptor) {
        this.prop      = prop;
        this.encryptor = encryptor;
    }

    /**
     * mid + ediDate + goodsAmt + merchantKey 를 DataEncrypt로 SHA-256 해시한 hex 문자열을 반환
     */
    public String makeEncData(String ediDate, String goodsAmt) {
        String raw = prop.getMid() + ediDate + goodsAmt + prop.getMerchantKey();
        System.out.println("▶ [PayService] raw = " + raw);
        String enc = encryptor.encrypt(raw);
        System.out.println("▶ [PayService] encData hex = " + enc);
        return enc;
    }
}