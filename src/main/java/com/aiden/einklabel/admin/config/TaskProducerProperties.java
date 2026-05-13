package com.aiden.einklabel.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "task-producer")
public class TaskProducerProperties {

    private String baseUrl = "http://127.0.0.1:18080";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
