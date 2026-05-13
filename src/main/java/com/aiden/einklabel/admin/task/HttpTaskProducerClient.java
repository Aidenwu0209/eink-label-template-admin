package com.aiden.einklabel.admin.task;

import com.aiden.einklabel.admin.config.TaskProducerProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import java.util.UUID;

@Service
public class HttpTaskProducerClient implements TaskProducerClient {

    private final RestClient restClient;

    public HttpTaskProducerClient(TaskProducerProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(normalizeBaseUrl(properties.getBaseUrl()))
                .build();
    }

    @Override
    public TaskProducerResponse dispatchAccessPointBind(ApBindShopTaskRequest request) {
        return post("/api/panpan/ap/bind-shop", request);
    }

    @Override
    public TaskProducerResponse dispatchTagUpdate(TagUpdateTaskRequest request) {
        return post("/api/panpan/tags/update", request);
    }

    @Override
    public TaskProducerResponse getTask(UUID taskUuid) {
        try {
            TaskProducerResponse response = restClient.get()
                    .uri("/api/tasks/{taskUuid}", taskUuid)
                    .retrieve()
                    .body(TaskProducerResponse.class);
            if (response == null) {
                throw new EruptWebApiRuntimeException("任务生产者返回空响应");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new EruptWebApiRuntimeException(
                    "任务生产者查询失败：" + ex.getStatusCode() + " " + ex.getResponseBodyAsString()
            );
        } catch (RestClientException ex) {
            throw new EruptWebApiRuntimeException("任务生产者不可用：" + ex.getMessage());
        }
    }

    private TaskProducerResponse post(String path, Object request) {
        try {
            TaskProducerResponse response = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TaskProducerResponse.class);
            if (response == null) {
                throw new EruptWebApiRuntimeException("任务生产者返回空响应");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new EruptWebApiRuntimeException(
                    "任务生产者请求失败：" + ex.getStatusCode() + " " + ex.getResponseBodyAsString()
            );
        } catch (RestClientException ex) {
            throw new EruptWebApiRuntimeException("任务生产者不可用：" + ex.getMessage());
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new EruptWebApiRuntimeException("任务生产者地址不能为空");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
