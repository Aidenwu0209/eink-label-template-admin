package com.aiden.einklabel.admin.task;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.aiden.einklabel.admin.esl.EslTagRecord;
import com.aiden.einklabel.admin.product.ProductRecord;
import com.aiden.einklabel.admin.store.StoreRecord;
import com.aiden.einklabel.admin.template.TemplateRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

@Service
public class TaskDispatchService {

    private final TaskProducerClient taskProducerClient;

    private final ObjectMapper objectMapper;

    public TaskDispatchService(TaskProducerClient taskProducerClient, ObjectMapper objectMapper) {
        this.taskProducerClient = taskProducerClient;
        this.objectMapper = objectMapper;
    }

    public TaskProducerResponse dispatchAccessPointShopBinding(AccessPointRecord accessPoint) {
        StoreRecord store = requireStore(accessPoint.getStore());
        ApBindShopTaskRequest request = new ApBindShopTaskRequest(
                UUID.randomUUID(),
                requireText(accessPoint.getSn(), "AP编码不能为空"),
                requireText(store.getCode(), "门店代码不能为空"),
                requireShopId(store),
                requireNumber(accessPoint.getShopNo(), "店内AP编号不能为空")
        );
        TaskProducerResponse response = taskProducerClient.dispatchAccessPointBind(request);
        rememberDispatch(accessPoint, response);
        return response;
    }

    public TaskProducerResponse dispatchLabelUpdate(EslTagRecord tag) {
        StoreRecord store = requireStore(tag.getStore());
        AccessPointRecord accessPoint = tag.getAccessPoint();
        if (accessPoint == null) {
            throw new EruptWebApiRuntimeException("电子价签必须先关联AP");
        }
        ProductRecord product = tag.getProduct();
        if (product == null) {
            throw new EruptWebApiRuntimeException("电子价签必须先绑定商品");
        }
        TemplateRecord template = product.getTemplate();
        if (template == null) {
            throw new EruptWebApiRuntimeException("商品必须先绑定模板");
        }

        int taskId = nextTaskId(tag.getLastTaskId());
        TagUpdateTaskRequest request = new TagUpdateTaskRequest(
                UUID.randomUUID(),
                requireText(store.getCode(), "门店代码不能为空"),
                requireText(accessPoint.getSn(), "AP编码不能为空"),
                String.valueOf(requireLong(tag.getTagId(), "价签ID不能为空")),
                resolveTemplateCode(template),
                screenCode(requireNumber(tag.getModel(), "价签型号不能为空")),
                tag.getModel(),
                Boolean.TRUE.equals(tag.getForceRefresh()) ? 1 : 0,
                productPayload(product),
                (long) taskId
        );
        TaskProducerResponse response = taskProducerClient.dispatchTagUpdate(request);
        tag.setLastTaskId(taskId);
        tag.setLastPreparedAt(LocalDateTime.now());
        tag.setLastUpdatePayload(toJson(request));
        rememberDispatch(tag, response);
        return response;
    }

    public TaskProducerResponse refreshAccessPointTaskStatus(AccessPointRecord accessPoint) {
        TaskProducerResponse response = taskProducerClient.getTask(requireTaskUuid(accessPoint.getLastProducerTaskUuid()));
        rememberDispatch(accessPoint, response);
        return response;
    }

    public TaskProducerResponse refreshLabelTaskStatus(EslTagRecord tag) {
        TaskProducerResponse response = taskProducerClient.getTask(requireTaskUuid(tag.getLastProducerTaskUuid()));
        rememberDispatch(tag, response);
        return response;
    }

    private StoreRecord requireStore(StoreRecord store) {
        if (store == null) {
            throw new EruptWebApiRuntimeException("请选择店铺");
        }
        requireText(store.getCode(), "门店代码不能为空");
        return store;
    }

    private Long requireShopId(StoreRecord store) {
        Integer shopId = store.getShopId();
        if (shopId == null) {
            throw new EruptWebApiRuntimeException("门店ID不能为空");
        }
        return shopId.longValue();
    }

    private Integer requireNumber(Integer value, String message) {
        if (value == null) {
            throw new EruptWebApiRuntimeException(message);
        }
        return value;
    }

    private Long requireLong(Long value, String message) {
        if (value == null) {
            throw new EruptWebApiRuntimeException(message);
        }
        return value;
    }

    private TaskProductPayload productPayload(ProductRecord product) {
        return new TaskProductPayload(
                requireText(firstText(product.getFullName(), product.getName()), "商品名称不能为空"),
                requireText(product.getCode(), "商品编码不能为空"),
                requireText(money(product.getPrice()), "商品价格不能为空"),
                product.getBrand(),
                product.getSpec(),
                product.getQrCode(),
                money(product.getPromoPrice())
        );
    }

    private String resolveTemplateCode(TemplateRecord template) {
        return requireText(
                firstText(template.getDeviceTemplateCode(), template.getName(), String.valueOf(template.getId())),
                "模板编码不能为空"
        );
    }

    private int nextTaskId(Integer currentTaskId) {
        int next = currentTaskId == null ? (int) (System.currentTimeMillis() & 0xffff) : currentTaskId + 1;
        if (next > 65535) {
            return 1;
        }
        return Math.max(next, 1);
    }

    private String screenCode(Integer model) {
        return String.format("%02X", model);
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

    private UUID requireTaskUuid(String taskUuid) {
        if (taskUuid == null || taskUuid.isBlank()) {
            throw new EruptWebApiRuntimeException("当前记录还没有提交过生产任务");
        }
        try {
            return UUID.fromString(taskUuid);
        } catch (IllegalArgumentException ex) {
            throw new EruptWebApiRuntimeException("最近生产任务UUID无效：" + taskUuid);
        }
    }

    private void rememberDispatch(AccessPointRecord accessPoint, TaskProducerResponse response) {
        if (response.taskUuid() != null) {
            accessPoint.setLastProducerTaskUuid(response.taskUuid().toString());
        }
        accessPoint.setLastDispatchStatus(response.status());
        accessPoint.setLastDispatchedAt(LocalDateTime.now());
    }

    private void rememberDispatch(EslTagRecord tag, TaskProducerResponse response) {
        if (response.taskUuid() != null) {
            tag.setLastProducerTaskUuid(response.taskUuid().toString());
        }
        tag.setLastDispatchStatus(response.status());
        tag.setLastDispatchedAt(LocalDateTime.now());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new EruptWebApiRuntimeException("任务数据无法序列化为JSON");
        }
    }
}
