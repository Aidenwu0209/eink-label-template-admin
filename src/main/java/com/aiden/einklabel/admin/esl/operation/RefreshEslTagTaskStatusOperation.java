package com.aiden.einklabel.admin.esl.operation;

import com.aiden.einklabel.admin.esl.EslTagRecord;
import com.aiden.einklabel.admin.esl.EslTagRecordRepository;
import com.aiden.einklabel.admin.task.TaskDispatchService;
import com.aiden.einklabel.admin.task.TaskProducerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

@Component
public class RefreshEslTagTaskStatusOperation implements OperationHandler<EslTagRecord, Void> {

    private final EslTagRecordRepository repository;
    private final TaskDispatchService taskDispatchService;
    private final ObjectMapper objectMapper;

    public RefreshEslTagTaskStatusOperation(
            EslTagRecordRepository repository,
            TaskDispatchService taskDispatchService,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.taskDispatchService = taskDispatchService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String exec(List<EslTagRecord> data, Void eruptForm, String[] param) {
        EslTagRecord tag = load(data);
        TaskProducerResponse response = taskDispatchService.refreshLabelTaskStatus(tag);
        repository.save(tag);
        return alert("已刷新商品更新任务状态", response);
    }

    private EslTagRecord load(List<EslTagRecord> data) {
        if (data == null || data.isEmpty()) {
            throw new EruptWebApiRuntimeException("请选择电子价签记录");
        }
        return repository.findById(data.get(0).getId())
                .orElseThrow(() -> new EruptWebApiRuntimeException("电子价签不存在"));
    }

    private String alert(String title, TaskProducerResponse response) {
        String stage = response.lastStatusStage() == null ? "" : "\\n阶段: " + response.lastStatusStage();
        return "alert(" + quote(title + "\\nTask UUID: " + response.taskUuid() + "\\n状态: " + response.status() + stage) + ");"
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
