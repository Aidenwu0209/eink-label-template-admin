package com.aiden.einklabel.admin.task;

public record TaskProductPayload(
        String productName,
        String productCode,
        String price,
        String brand,
        String spec,
        String qrContent,
        String promoPrice
) {
}
