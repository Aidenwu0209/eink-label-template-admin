package com.aiden.einklabel.admin.esl.operation;

import com.aiden.einklabel.admin.esl.EslTagRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;

@Component
public class OpenEslTagUpdateCommandOperation implements OperationHandler<EslTagRecord, Void> {

    private final ObjectMapper objectMapper;

    public OpenEslTagUpdateCommandOperation(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String exec(List<EslTagRecord> data, Void eruptForm, String[] param) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return "window.open(" + quote("/api/esl-labels/" + data.get(0).getId() + "/update-command") + ", '_blank');";
    }

    private String quote(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to encode URL", e);
        }
    }
}
