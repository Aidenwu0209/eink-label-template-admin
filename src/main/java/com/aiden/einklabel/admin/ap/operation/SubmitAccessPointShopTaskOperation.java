package com.aiden.einklabel.admin.ap.operation;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.aiden.einklabel.admin.ap.AccessPointRecordRepository;
import com.aiden.einklabel.admin.task.TaskDispatchService;
import com.aiden.einklabel.admin.task.TaskProducerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

@Component
public class SubmitAccessPointShopTaskOperation implements OperationHandler<AccessPointRecord, Void> {

    private final AccessPointRecordRepository repository;
    private final TaskDispatchService taskDispatchService;
    private final ObjectMapper objectMapper;

    public SubmitAccessPointShopTaskOperation(
            AccessPointRecordRepository repository,
            TaskDispatchService taskDispatchService,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.taskDispatchService = taskDispatchService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String exec(List<AccessPointRecord> data, Void eruptForm, String[] param) {
        AccessPointRecord accessPoint = load(data);
        TaskProducerResponse response = taskDispatchService.dispatchAccessPointShopBinding(accessPoint);
        repository.save(accessPoint);
        return alert("已提交门店配置任务", response);
    }

    private AccessPointRecord load(List<AccessPointRecord> data) {
        if (data == null || data.isEmpty()) {
            throw new EruptWebApiRuntimeException("请选择AP记录");
        }
        return repository.findById(data.get(0).getId())
                .orElseThrow(() -> new EruptWebApiRuntimeException("AP不存在"));
    }

    private String alert(String title, TaskProducerResponse response) {
        return "alert(" + quote(title + "\\nTask UUID: " + response.taskUuid() + "\\n状态: " + response.status()) + ");"
                + "window.location.reload();";
    }

    private String quote(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to encode operation message", e);
        }
    }
}
