package com.aiden.einklabel.admin.template;

import com.aiden.einklabel.admin.config.TemplateEditorProperties;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TemplateEditorLinkService {

    private final TemplateEditorProperties properties;

    public TemplateEditorLinkService(TemplateEditorProperties properties) {
        this.properties = properties;
    }

    public String buildEditorUrl(TemplateRecord template) {
        Objects.requireNonNull(template.getId(), "template id is required before opening the editor");
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .queryParam("templateId", template.getId())
                .queryParam("templateName", template.getName())
                .queryParam("width", template.getWidth())
                .queryParam("height", template.getHeight())
                .queryParam("colorMode", template.getColorMode())
                .queryParam("apiBase", properties.getApiBaseUrl())
                .queryParam("saveApi", properties.getSaveApiUrl())
                .queryParam("saveExportMode", properties.getSaveExportMode())
                .queryParam("locale", properties.getLocale())
                .queryParam("market", properties.getMarket())
                .build()
                .encode()
                .toUriString();
    }
}
