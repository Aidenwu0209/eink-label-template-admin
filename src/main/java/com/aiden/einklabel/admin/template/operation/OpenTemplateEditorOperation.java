package com.aiden.einklabel.admin.template.operation;

import com.aiden.einklabel.admin.template.TemplateEditorLinkService;
import com.aiden.einklabel.admin.template.TemplateRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;

@Component
public class OpenTemplateEditorOperation implements OperationHandler<TemplateRecord, Void> {

    private final TemplateEditorLinkService linkService;

    private final ObjectMapper objectMapper;

    public OpenTemplateEditorOperation(TemplateEditorLinkService linkService, ObjectMapper objectMapper) {
        this.linkService = linkService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String exec(List<TemplateRecord> data, Void eruptForm, String[] param) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        String url = linkService.buildEditorUrl(data.get(0));
        return "window.open(" + quote(url) + ", '_blank');";
    }

    private String quote(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to encode editor URL", e);
        }
    }
}
