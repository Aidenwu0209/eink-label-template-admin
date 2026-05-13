package com.aiden.einklabel.admin.task;

import java.util.UUID;

public record ApBindShopTaskRequest(
        UUID taskUuid,
        String apCode,
        String shopCode,
        Long shopId,
        Integer shopNo
) {
}
