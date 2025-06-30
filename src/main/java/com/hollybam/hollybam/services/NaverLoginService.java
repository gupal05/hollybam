package com.hollybam.hollybam.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class NaverLoginService {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.redirect.uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAccessToken(String code, String state) throws IOException {
        String apiURL = "https://nid.naver.com/oauth2.0/token?" +
                "grant_type=authorization_code" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&code=" + code +
                "&state=" + state;

        URL url = new URL(apiURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String response = br.lines().collect(Collectors.joining());
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        }
    }

    public JsonNode getUserInfo(String accessToken) throws IOException {
        String apiURL = "https://openapi.naver.com/v1/nid/me";
        URL url = new URL(apiURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String response = br.lines().collect(Collectors.joining());
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("response"); // 유저 정보 노드
        }
    }
}

