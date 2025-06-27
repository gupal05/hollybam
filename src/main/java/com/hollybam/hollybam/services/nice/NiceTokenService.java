package com.hollybam.hollybam.services.nice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

/**
 * NICE 평가정보 연동을 위한 토큰 관리 서비스 클래스입니다.
 * - Access Token을 발급받아 인증에 사용합니다.
 * - 해당 토큰은 NICE API 호출 시 필요한 Authorization Header에 포함됩니다.
 *
 * 이 클래스는 Spring의 Service로 등록되어 있으며,
 * 컨트롤러 또는 다른 서비스에서 주입받아 사용할 수 있습니다.
 */
@Slf4j // 로그를 위한 Lombok 어노테이션 (log.info 등 사용 가능)
@Service // Spring에서 Bean으로 등록하기 위한 어노테이션
public class NiceTokenService {

    /**
     * NICE 평가정보로부터 Access Token을 발급받는 메서드입니다.
     *
     * @return 발급된 Access Token 문자열
     * @throws Exception 네트워크 통신 오류, 응답 파싱 오류 등이 발생할 경우 예외 발생
     */
    public String getAccessToken() throws Exception {
        // [1] 토큰 요청을 보낼 NICE의 OAuth 2.0 토큰 발급 URL
        String url = "https://svc.niceapi.co.kr:22001/digital/niceid/oauth/oauth/token";

        // [2] NICE 측에서 발급받은 클라이언트 아이디 (Client ID)
        String clientId = "3fe07cd0-239c-4286-a47b-83679e5a4cdc";

        // [3] NICE 측에서 발급받은 클라이언트 비밀번호 (Client Secret)
        String clientSecret = "5a36eb10106d18961b67ab4dcdedff7a";

        // [4] 인증 정보를 base64로 인코딩: "client_id:client_secret" 형식
        // HTTP Basic 인증 방식에 맞는 Authorization 헤더 생성
        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        // [5] POST 요청 객체 생성 (form-urlencoded 형식 사용)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url)) // 요청할 URL 지정
                .header("Content-Type", "application/x-www-form-urlencoded") // Content-Type 지정
                .header("Authorization", "Basic " + auth) // Basic 인증 정보 포함
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&scope=default")) // POST 바디 내용
                .build();

        // [6] HttpClient 인스턴스 생성 (기본 설정 사용)
        HttpClient client = HttpClient.newHttpClient();

        // [7] 요청 실행 및 응답 수신 (동기 방식)
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // [8] 응답 본문(JSON 문자열)을 Jackson의 ObjectMapper로 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.body());

        // [9] JSON 응답 중 access_token 값만 추출
        String token = json.path("dataBody").path("access_token").asText();

        // [10] 최종적으로 access_token 반환
        return token;
    }
}
