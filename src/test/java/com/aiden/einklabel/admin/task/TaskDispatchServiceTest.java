package com.aiden.einklabel.admin.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.aiden.einklabel.admin.esl.EslTagRecord;
import com.aiden.einklabel.admin.product.ProductRecord;
import com.aiden.einklabel.admin.store.StoreRecord;
import com.aiden.einklabel.admin.template.TemplateRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TaskDispatchServiceTest {

    private final FakeTaskProducerClient client = new FakeTaskProducerClient();

    private final TaskDispatchService service = new TaskDispatchService(client, new ObjectMapper());

    @Test
    void dispatchesAccessPointBindingThroughProducerApi() {
        StoreRecord store = new StoreRecord();
        store.setCode("ZH01");
        store.setShopId(88);

        AccessPointRecord accessPoint = new AccessPointRecord();
        accessPoint.setStore(store);
        accessPoint.setSn("ESLAP00000009");
        accessPoint.setShopNo(3);

        service.dispatchAccessPointShopBinding(accessPoint);

        assertThat(client.lastAccessPointRequest).isNotNull();
        assertThat(client.lastAccessPointRequest.apCode()).isEqualTo("ESLAP00000009");
        assertThat(client.lastAccessPointRequest.shopCode()).isEqualTo("ZH01");
        assertThat(client.lastAccessPointRequest.shopId()).isEqualTo(88L);
        assertThat(client.lastAccessPointRequest.shopNo()).isEqualTo(3);
    }

    @Test
    void dispatchesLabelUpdateAndStoresLastPreparedPayload() {
        StoreRecord store = new StoreRecord();
        store.setCode("ZH01");
        store.setShopId(88);

        AccessPointRecord accessPoint = new AccessPointRecord();
        accessPoint.setStore(store);
        accessPoint.setSn("ESLAP00000009");

        TemplateRecord template = new TemplateRecord();
        template.setId(12L);
        template.setName("默认价签");
        template.setDeviceTemplateCode("PRICEPROMO");

        ProductRecord product = new ProductRecord();
        product.setStore(store);
        product.setCode("6902538004045");
        product.setName("脉动维生素饮料");
        product.setFullName("脉动 维生素饮料青柠口味 600ML");
        product.setBrand("脉动");
        product.setSpec("600ML");
        product.setPrice(new BigDecimal("10.8"));
        product.setPromoPrice(new BigDecimal("8.5"));
        product.setQrCode("esl.wdyc.cn");
        product.setTemplate(template);

        EslTagRecord tag = new EslTagRecord();
        tag.setStore(store);
        tag.setAccessPoint(accessPoint);
        tag.setTagId(6597069770841L);
        tag.setModel(6);
        tag.setForceRefresh(true);
        tag.setLastTaskId(39137);
        tag.setProduct(product);

        service.dispatchLabelUpdate(tag);

        assertThat(client.lastTagRequest).isNotNull();
        assertThat(client.lastTagRequest.shopCode()).isEqualTo("ZH01");
        assertThat(client.lastTagRequest.apCode()).isEqualTo("ESLAP00000009");
        assertThat(client.lastTagRequest.tagId()).isEqualTo("6597069770841");
        assertThat(client.lastTagRequest.templateName()).isEqualTo("PRICEPROMO");
        assertThat(client.lastTagRequest.screenCode()).isEqualTo("06");
        assertThat(client.lastTagRequest.modelDecimal()).isEqualTo(6);
        assertThat(client.lastTagRequest.forceRefresh()).isEqualTo(1);
        assertThat(client.lastTagRequest.vendorTaskId()).isEqualTo(39138L);
        assertThat(client.lastTagRequest.product().productName()).isEqualTo("脉动 维生素饮料青柠口味 600ML");
        assertThat(client.lastTagRequest.product().productCode()).isEqualTo("6902538004045");
        assertThat(client.lastTagRequest.product().price()).isEqualTo("10.80");
        assertThat(client.lastTagRequest.product().promoPrice()).isEqualTo("8.50");
        assertThat(tag.getLastTaskId()).isEqualTo(39138);
        assertThat(tag.getLastPreparedAt()).isNotNull();
        assertThat(tag.getLastUpdatePayload()).contains("\"templateName\":\"PRICEPROMO\"");
    }

    private static class FakeTaskProducerClient implements TaskProducerClient {

        private ApBindShopTaskRequest lastAccessPointRequest;

        private TagUpdateTaskRequest lastTagRequest;

        @Override
        public TaskProducerResponse dispatchAccessPointBind(ApBindShopTaskRequest request) {
            this.lastAccessPointRequest = request;
            return response();
        }

        @Override
        public TaskProducerResponse dispatchTagUpdate(TagUpdateTaskRequest request) {
            this.lastTagRequest = request;
            return response();
        }

        private TaskProducerResponse response() {
            return new TaskProducerResponse(null, null, null, "QUEUED", null, null, 0, null, null, null, null);
        }
    }
}
