package com.aiden.einklabel.admin.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "template-editor")
public class TemplateEditorProperties {

    private String baseUrl = "http://127.0.0.1:5173/";

    private String apiBaseUrl = "http://127.0.0.1:8080/api";

    private String saveApiUrl = "http://127.0.0.1:8080/api/template/save";

    private String locale = "zh-CN";

    private String market = "CN";

    private String saveExportMode = "fabric-json";

    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://127.0.0.1:5173",
            "http://localhost:5173",
            "http://127.0.0.1:5174",
            "http://localhost:5174",
            "http://127.0.0.1:4173",
            "http://localhost:4173"
    ));

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getSaveApiUrl() {
        return saveApiUrl;
    }

    public void setSaveApiUrl(String saveApiUrl) {
        this.saveApiUrl = saveApiUrl;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getSaveExportMode() {
        return saveExportMode;
    }

    public void setSaveExportMode(String saveExportMode) {
        this.saveExportMode = saveExportMode;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
