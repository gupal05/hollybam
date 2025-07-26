package com.hollybam.hollybam.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "payster")
@Data
public class PaysterProperties {
    private String mid;
    private String merchantKey;
    private String baseUrl;
    private String urlInit;
    private String urlApprove;
    private String urlCancel;
    private String returnUrl;
    private String notiUrl;
}