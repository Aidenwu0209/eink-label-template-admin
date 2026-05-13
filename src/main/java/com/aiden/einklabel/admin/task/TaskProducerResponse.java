package com.aiden.einklabel.admin.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskProducerResponse(
        UUID taskUuid,
        String messageType,
        String brand,
        String status,
        JsonNode payload,
        String errorMessage,
        int retryCount,
        Instant createdAt,
        Instant updatedAt,
        Instant queuedAt,
        Instant lastRetriedAt
) {
}
