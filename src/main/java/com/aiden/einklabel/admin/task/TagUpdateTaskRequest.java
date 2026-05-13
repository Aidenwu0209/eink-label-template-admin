package com.aiden.einklabel.admin.task;

import java.util.UUID;

public record TagUpdateTaskRequest(
        UUID taskUuid,
        String shopCode,
        String apCode,
        String tagId,
        String templateName,
        String screenCode,
        Integer modelDecimal,
        Integer forceRefresh,
        TaskProductPayload product,
        Long vendorTaskId
) {
}
