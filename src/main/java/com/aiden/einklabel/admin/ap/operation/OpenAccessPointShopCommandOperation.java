package com.aiden.einklabel.admin.ap.operation;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;

@Component
public class OpenAccessPointShopCommandOperation implements OperationHandler<AccessPointRecord, Void> {

    private final ObjectMapper objectMapper;

    public OpenAccessPointShopCommandOperation(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String exec(List<AccessPointRecord> data, Void eruptForm, String[] param) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return "window.open(" + quote("/api/access-points/" + data.get(0).getId() + "/shop-binding-command") + ", '_blank');";
    }

    private String quote(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to encode URL", e);
        }
    }
}
