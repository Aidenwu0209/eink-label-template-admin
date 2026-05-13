package com.aiden.einklabel.admin.task;

public interface TaskProducerClient {

    TaskProducerResponse dispatchAccessPointBind(ApBindShopTaskRequest request);

    TaskProducerResponse dispatchTagUpdate(TagUpdateTaskRequest request);
}
