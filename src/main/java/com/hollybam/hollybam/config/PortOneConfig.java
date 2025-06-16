package com.hollybam.hollybam.config;

import com.siot.IamportRestClient.IamportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortOneConfig {

    @Value("${portone.api_key}")
    private String apiKey;

    @Value("${portone.api_secret}")
    private String secretKey;

    // 포트원 객체에다 내 액세스, 시크릿 키를 넣어 내 가맹점 정보로 초기화 시킨 것
    @Bean
    public IamportClient iamportClient(){
        System.out.println("PortOne Config - API Key: " + apiKey);
        System.out.println("PortOne Config - Secret Key: " + secretKey);
        return new IamportClient(apiKey, secretKey);
    }
}