package com.hollybam.hollybam.services.nice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hollybam.hollybam.util.NiceCryptoUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * NICE 본인인증 암호화 토큰 요청 서비스
 * 2단계: 암호화에 필요한 토큰 정보(token_val 등)를 발급받는 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NiceCryptoTokenService {

    private final NiceTokenService niceTokenService;
    private final HttpSession session;

    @Value("${nice.client-id}")
    private String clientId;

    @Value("${nice.product-id}")
    private String productId;

    @Value("${nice.return-url}")
    private String returnUrl;

    @Value("${nice.naver-return-url:#{null}}")
    private String naverReturnUrl;

    @Value("${nice.google-return-url:#{null}}")
    private String googleReturnUrl;

    /**
     * 기본 본인인증용 토큰 요청
     */
    public Map<String, String> requestCryptoToken() throws Exception {
        return requestCryptoToken(returnUrl);
    }

    /**
     * 네이버 로그인용 본인인증 토큰 요청
     */
    public Map<String, String> requestCryptoTokenForNaver() throws Exception {
        String useReturnUrl = naverReturnUrl != null ?
                naverReturnUrl : returnUrl.replace("/nice/result", "/naver/nice/result");
        return requestCryptoToken(useReturnUrl);
    }

    /**
     * 구글 로그인용 본인인증 토큰 요청
     */
    public Map<String, String> requestCryptoTokenForGoogle() throws Exception {
        String useReturnUrl = googleReturnUrl != null ?
                googleReturnUrl : returnUrl.replace("/nice/result", "/google/nice/result");
        return requestCryptoToken(useReturnUrl);
    }

    /**
     * 공통 토큰 요청 메서드
     */
    private Map<String, String> requestCryptoToken(String customReturnUrl) throws Exception {
        // [1] 날짜 및 요청번호 생성
        String req_dtim = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String req_no = "REQ" + req_dtim + ((int)(Math.random() * 9000) + 1000);

        // [2] access_token 가져오기
        String accessToken = niceTokenService.getAccessToken();

        // [3] 인증 문자열 생성 (Base64(accessToken:timestamp:clientId))
        long timestamp = System.currentTimeMillis() / 1000;
        String authString = accessToken + ":" + timestamp + ":" + clientId;
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());

        // [4] 요청 바디 구성
        String requestJson = String.format(
                "{\"dataHeader\":{\"CNTY_CD\":\"kr\"},\"dataBody\":{\"req_dtim\":\"%s\",\"req_no\":\"%s\",\"enc_mode\":\"1\", \"auth_type\":\"S\"}}",
                req_dtim, req_no
        );

        // [5] HTTP 요청 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://svc.niceapi.co.kr:22001/digital/niceid/api/v1.0/common/crypto/token"))
                .header("Content-Type", "application/json")
                .header("Authorization", "bearer " + encodedAuth)
                .header("ProductID", productId)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        // [6] 요청 실행
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // [7] 응답 상태 확인
        if (response.statusCode() != 200) {
            log.error("NICE API 요청 실패: {}, 응답: {}", response.statusCode(), response.body());
            throw new RuntimeException("NICE API 요청 실패: " + response.statusCode());
        }

        // [8] 응답 파싱
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        // 응답 성공 여부 확인
        JsonNode dataHeader = root.path("dataHeader");
        String resultCode = dataHeader.path("GW_RSLT_CD").asText();
        if (!"1200".equals(resultCode)) {
            log.error("NICE API 응답 실패: {}, 메시지: {}",
                    resultCode, dataHeader.path("GW_RSLT_MSG").asText());
            throw new RuntimeException("NICE API 응답 실패: " + resultCode);
        }

        JsonNode dataBody = root.path("dataBody");

        // [9] 필요한 값 추출
        String tokenVal = dataBody.path("token_val").asText();
        String tokenVersionId = dataBody.path("token_version_id").asText();
        String siteCode = dataBody.path("site_code").asText();

        // [10] 세션에 저장 (보안을 위해 만료시간 설정)
        session.setAttribute("token_val", tokenVal);
        session.setAttribute("req_dtim", req_dtim);
        session.setAttribute("req_no", req_no);
        session.setMaxInactiveInterval(30 * 60); // 30분 후 세션 만료

        // [11] 결과 반환
        Map<String, String> result = new HashMap<>();
        result.put("req_dtim", req_dtim);
        result.put("req_no", req_no);
        result.put("token_val", tokenVal);
        result.put("token_version_id", tokenVersionId);
        result.put("site_code", siteCode);

        // [12] enc_data, integrity_value 생성
        String resultForHash = req_dtim.trim() + req_no.trim() + tokenVal.trim();
        String resultHash = NiceCryptoUtil.encryptSHA256(resultForHash);

        // key, iv, hmac_key 생성
        String key = resultHash.substring(0, 16);
        String iv = resultHash.substring(resultHash.length() - 16);
        String hmacKey = resultHash.substring(0, 32);

        // 인증 요청 파라미터 (JSON 문자열)
        String plainJson = String.format(
                "{\"requestno\":\"%s\",\"returnurl\":\"%s\",\"sitecode\":\"%s\", \"auth_type\":\"S\"}",
                req_no,
                customReturnUrl, // 파라미터로 받은 URL 사용
                siteCode
        );

        // enc_data 생성
        String encData = NiceCryptoUtil.encryptAES(plainJson, key, iv);

        // integrity_value 생성
        String integrityValue = NiceCryptoUtil.hmac256(hmacKey, encData);

        // [13] 결과에 enc_data, integrity_value 추가
        result.put("enc_data", encData);
        result.put("integrity_value", integrityValue);

        log.info("NICE 토큰 생성 완료 - returnUrl: {}", customReturnUrl);

        return result;
    }
}