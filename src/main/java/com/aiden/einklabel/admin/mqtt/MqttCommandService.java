package com.aiden.einklabel.admin.mqtt;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.aiden.einklabel.admin.esl.EslTagRecord;
import com.aiden.einklabel.admin.product.ProductRecord;
import com.aiden.einklabel.admin.store.StoreRecord;
import com.aiden.einklabel.admin.template.TemplateRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

@Service
public class MqttCommandService {

    private final ObjectMapper objectMapper;

    public MqttCommandService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public MqttCommand buildAccessPointShopBindingCommand(AccessPointRecord accessPoint) {
        StoreRecord store = requireStore(accessPoint.getStore());
        String apCode = requireText(accessPoint.getSn(), "AP编码不能为空");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("op", "shopcode");
        payload.put("shopcode", store.getCode());
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("id", UUID.randomUUID().toString());
        payload.put("shopid", store.getShopId());
        payload.put("shopno", accessPoint.getShopNo());
        return new MqttCommand("esl/server/mgr/" + apCode, payload);
    }

    public MqttCommand buildLabelUpdateCommand(EslTagRecord tag) {
        StoreRecord store = requireStore(tag.getStore());
        ProductRecord product = tag.getProduct();
        if (product == null) {
            throw new EruptWebApiRuntimeException("电子价签必须先绑定商品");
        }
        TemplateRecord template = product.getTemplate();
        if (template == null) {
            throw new EruptWebApiRuntimeException("商品必须先绑定模板");
        }

        Map<String, Object> value = buildProductValue(product);
        String checksum = md5(toJson(value));
        int taskId = nextTaskId(tag.getLastTaskId());

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("tag", tag.getTagId());
        item.put("tmpl", resolveTemplateCode(template));
        item.put("model", tag.getModel());
        item.put("checksum", checksum);
        item.put("forcefrash", Boolean.TRUE.equals(tag.getForceRefresh()) ? 1 : 0);
        item.put("value", value);
        item.put("taskid", taskId);
        item.put("token", tag.getToken() == null ? 0 : tag.getToken());

        List<Map<String, Object>> data = new ArrayList<>();
        data.add(item);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("command", "wtag");
        payload.put("data", data);
        payload.put("id", UUID.randomUUID().toString());
        payload.put("timestamp", Instant.now().toEpochMilli() / 1000.0);
        payload.put("shop", store.getCode());

        return new MqttCommand("esl/server/data/" + store.getCode(), payload);
    }

    public Map<String, Object> buildProductValue(ProductRecord product) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("GOODS_NAME", firstText(product.getFullName(), product.getName()));
        value.put("GOODS_CODE", product.getCode());
        value.put("F_1", money(product.getPrice()));
        value.put("F_2", product.getBrand());
        value.put("F_3", product.getName());
        value.put("F_4", product.getSpec());
        value.put("F_5", product.getFullName());
        value.put("F_6", product.getOrigin());
        value.put("F_7", product.getField7());
        value.put("F_8", product.getPromoStart());
        value.put("F_9", product.getPromoEnd());
        value.put("QRCODE", product.getQrCode());
        value.put("F_11", product.getQualityInspector());
        value.put("F_20", money(product.getPromoPrice()));
        return value;
    }

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new EruptWebApiRuntimeException("MQTT数据无法序列化为JSON");
        }
    }

    public int extractTaskId(MqttCommand command) {
        Object data = command.payload().get("data");
        if (data instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
            Object taskId = first.get("taskid");
            if (taskId instanceof Number number) {
                return number.intValue();
            }
        }
        throw new EruptWebApiRuntimeException("MQTT数据缺少taskid");
    }

    private StoreRecord requireStore(StoreRecord store) {
        if (store == null) {
            throw new EruptWebApiRuntimeException("请选择店铺");
        }
        requireText(store.getCode(), "门店代码不能为空");
        return store;
    }

    private String resolveTemplateCode(TemplateRecord template) {
        return firstText(template.getDeviceTemplateCode(), template.getName(), String.valueOf(template.getId()));
    }

    private int nextTaskId(Integer currentTaskId) {
        int next = currentTaskId == null ? (int) (System.currentTimeMillis() & 0xffff) : currentTaskId + 1;
        if (next > 65535) {
            return 1;
        }
        return Math.max(next, 1);
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new EruptWebApiRuntimeException(message);
        }
        return value;
    }

    private String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm is unavailable", e);
        }
    }
}
