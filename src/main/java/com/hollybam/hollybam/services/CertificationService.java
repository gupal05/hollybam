package com.hollybam.hollybam.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hollybam.hollybam.dto.CertificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class CertificationService {

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

        String birthdayStr = responseNode.path("birthday").asText(null);  // "1998-10-26" 형태
        boolean certified = responseNode.path("certified").asBoolean(false);

        LocalDate birthday = null;
        if (birthdayStr != null && !birthdayStr.isBlank()) {
            birthday = LocalDate.parse(birthdayStr);  // ISO_LOCAL_DATE 형식 파싱
        }

        int age = (birthday != null) ? Period.between(birthday, LocalDate.now()).getYears() : 0;
        boolean isAdult = certified && age >= 19;

        String name = responseNode.path("name").asText();
        String phone = responseNode.path("phone").asText();

        return new CertificationDto(isAdult, name, phone, birthday);
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
}


