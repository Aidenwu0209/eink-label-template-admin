package com.aiden.einklabel.admin.mqtt;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiden.einklabel.admin.esl.EslTagRecord;
import com.aiden.einklabel.admin.product.ProductRecord;
import com.aiden.einklabel.admin.store.StoreRecord;
import com.aiden.einklabel.admin.template.TemplateRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MqttCommandServiceTest {

    private final MqttCommandService service = new MqttCommandService(new ObjectMapper());

    @Test
    void buildsLabelUpdateCommandFromBoundProductAndTemplate() {
        StoreRecord store = new StoreRecord();
        store.setCode("ZH01");
        store.setShopId(1);

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
        tag.setTagId(6597069770841L);
        tag.setModel(6);
        tag.setForceRefresh(true);
        tag.setLastTaskId(39137);
        tag.setToken(161986);
        tag.setProduct(product);

        MqttCommand command = service.buildLabelUpdateCommand(tag);

        assertThat(command.topic()).isEqualTo("esl/server/data/ZH01");
        assertThat(command.payload()).containsEntry("command", "wtag");
        assertThat(command.payload()).containsEntry("shop", "ZH01");

        List<?> data = (List<?>) command.payload().get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> item = (Map<String, Object>) data.get(0);
        assertThat(item).containsEntry("tag", 6597069770841L);
        assertThat(item).containsEntry("tmpl", "PRICEPROMO");
        assertThat(item).containsEntry("model", 6);
        assertThat(item).containsEntry("forcefrash", 1);
        assertThat(item).containsEntry("taskid", 39138);
        assertThat(item).containsEntry("token", 161986);

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) item.get("value");
        assertThat(value).containsEntry("GOODS_NAME", "脉动 维生素饮料青柠口味 600ML");
        assertThat(value).containsEntry("GOODS_CODE", "6902538004045");
        assertThat(value).containsEntry("F_1", "10.80");
        assertThat(value).containsEntry("F_20", "8.50");
        assertThat(value).containsEntry("QRCODE", "esl.wdyc.cn");
        assertThat(item.get("checksum")).asString().hasSize(32);
    }

    @Test
    void buildsAccessPointShopBindingCommand() {
        StoreRecord store = new StoreRecord();
        store.setCode("ZH01");
        store.setShopId(1);

        com.aiden.einklabel.admin.ap.AccessPointRecord accessPoint = new com.aiden.einklabel.admin.ap.AccessPointRecord();
        accessPoint.setStore(store);
        accessPoint.setSn("ESLAP00000009");
        accessPoint.setShopNo(3);

        MqttCommand command = service.buildAccessPointShopBindingCommand(accessPoint);

        assertThat(command.topic()).isEqualTo("esl/server/mgr/ESLAP00000009");
        assertThat(command.payload()).containsEntry("op", "shopcode");
        assertThat(command.payload()).containsEntry("shopcode", "ZH01");
        assertThat(command.payload()).containsEntry("shopid", 1);
        assertThat(command.payload()).containsEntry("shopno", 3);
    }
}
