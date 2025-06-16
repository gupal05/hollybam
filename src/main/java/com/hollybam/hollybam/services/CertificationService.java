package com.hollybam.hollybam.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hollybam.hollybam.dao.IF_CertificationDao;
import com.hollybam.hollybam.dao.IF_LoginDao;
import com.hollybam.hollybam.dto.CertificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificationService {
    private final IF_CertificationDao certificationDao;

    private final String restApiKey = "3524268181084271";
    private final String restApiSecret = "87VDgykHU6vua41v5f8Ofxvr6qiWiMegYdFlCh7xeDEGST9TG9PvMDPW1jTvm81lyoDXuQITwizuP6gf";

    public CertificationDto verifyAdultByImpUid(String impUid) throws IOException, InterruptedException {
        String accessToken = getAccessToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.iamport.kr/certifications/" + impUid))
                .header("Authorization", accessToken)
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode responseNode = root.path("response");

        String birthdayStr = responseNode.path("birthday").asText(null);  // "1998-10-26"
        boolean certified = responseNode.path("certified").asBoolean(false);

        LocalDate birthday = null;
        if (birthdayStr != null && !birthdayStr.isBlank()) {
            birthday = LocalDate.parse(birthdayStr);
        }

        int age = (birthday != null) ? Period.between(birthday, LocalDate.now()).getYears() : 0;
        boolean isAdult = certified && age >= 19;

        String name = responseNode.path("name").asText();
        String phone = responseNode.path("phone").asText();
        String gender = responseNode.path("gender").asText("UNKNOWN"); // 'male', 'female', 혹은 'UNKNOWN'

        // 만 19세 미만 차단을 더 엄격하게
        if (!certified) {
            throw new IllegalStateException("본인인증이 완료되지 않았습니다.");
        }

        if (age < 19) {
            // 로그 기록 (법적 요구사항)
            logMinorAccessAttempt(name, phone, birthday);
            throw new IllegalStateException("만 19세 미만은 접근할 수 없습니다.");
        }

        return new CertificationDto(isAdult, name, phone, birthday, gender);
    }

    private void logMinorAccessAttempt(String name, String phone, LocalDate birthday) {
        // 미성년자 접근 시도 로그 기록 (법적 의무)
        log.warn("미성년자 접근 시도 - 생년월일: {}, 전화번호: {}", birthday,
                phone.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-****-$3"));
    }

    private String getAccessToken() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String json = "{\"imp_key\":\"" + restApiKey + "\",\"imp_secret\":\"" + restApiSecret + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.iamport.kr/users/getToken"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.body());

        return node.path("response").path("access_token").asText();
    }

    public void saveGuestUser(String uuid, CertificationDto dto) {
        if (certificationDao.existsByUuid(uuid) == 0) {
            certificationDao.insertGuestUser(Map.of(
                    "uuid", uuid,
                    "birth", dto.getMemberBirth(),
                    "gender", convertGenderToKorean(dto.getGender()), // ⬅ 여기서 한글로 변환
                    "phone", dto.getMemberPhone(),
                    "isAdult", dto.isAdult() ? 1 : 0
            ));
        }
    }


    private String convertGenderToKorean(String gender) {
        if ("male".equalsIgnoreCase(gender)) {
            return "남자";
        } else if ("female".equalsIgnoreCase(gender)) {
            return "여자";
        } else {
            return "알수없음";
        }
    }


}


