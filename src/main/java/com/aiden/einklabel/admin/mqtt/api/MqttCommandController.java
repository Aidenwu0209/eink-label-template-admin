package com.aiden.einklabel.admin.mqtt.api;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.aiden.einklabel.admin.ap.AccessPointRecordRepository;
import com.aiden.einklabel.admin.esl.EslTagRecord;
import com.aiden.einklabel.admin.esl.EslTagRecordRepository;
import com.aiden.einklabel.admin.mqtt.MqttCommand;
import com.aiden.einklabel.admin.mqtt.MqttCommandService;
import com.aiden.einklabel.admin.org.OrganizationAccessService;
import com.aiden.einklabel.admin.product.ProductRecord;
import com.aiden.einklabel.admin.product.ProductRecordRepository;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.upms.annotation.EruptLoginAuth;

@RestController
@RequestMapping("/api")
public class MqttCommandController {

    private final AccessPointRecordRepository accessPointRepository;

    private final EslTagRecordRepository tagRepository;

    private final ProductRecordRepository productRepository;

    private final MqttCommandService mqttCommandService;

    private final OrganizationAccessService organizationAccessService;

    public MqttCommandController(
            AccessPointRecordRepository accessPointRepository,
            EslTagRecordRepository tagRepository,
            ProductRecordRepository productRepository,
            MqttCommandService mqttCommandService,
            OrganizationAccessService organizationAccessService
    ) {
        this.accessPointRepository = accessPointRepository;
        this.tagRepository = tagRepository;
        this.productRepository = productRepository;
        this.mqttCommandService = mqttCommandService;
        this.organizationAccessService = organizationAccessService;
    }

    @GetMapping("/access-points/{id}/shop-binding-command")
    @EruptLoginAuth
    public ResponseEntity<MqttCommand> getAccessPointShopBindingCommand(@PathVariable Long id) {
        AccessPointRecord accessPoint = accessPointRepository.findById(id)
                .orElseThrow(() -> new EruptWebApiRuntimeException("AP不存在"));
        organizationAccessService.assertCanAccess(accessPoint);
        return ResponseEntity.ok(mqttCommandService.buildAccessPointShopBindingCommand(accessPoint));
    }

    @GetMapping("/esl-labels/{id}/update-command")
    @EruptLoginAuth
    @Transactional
    public ResponseEntity<MqttCommand> getLabelUpdateCommand(@PathVariable Long id) {
        EslTagRecord tag = tagRepository.findById(id)
                .orElseThrow(() -> new EruptWebApiRuntimeException("电子价签不存在"));
        organizationAccessService.assertCanAccess(tag);
        MqttCommand command = mqttCommandService.buildLabelUpdateCommand(tag);
        rememberPreparedCommand(tag, command);
        return ResponseEntity.ok(command);
    }

    @PostMapping("/esl-labels/{labelId}/bind-product/{productId}")
    @EruptLoginAuth
    @Transactional
    public ResponseEntity<Map<String, Object>> bindProduct(
            @PathVariable Long labelId,
            @PathVariable Long productId
    ) {
        EslTagRecord tag = tagRepository.findById(labelId)
                .orElseThrow(() -> new EruptWebApiRuntimeException("电子价签不存在"));
        ProductRecord product = productRepository.findById(productId)
                .orElseThrow(() -> new EruptWebApiRuntimeException("商品不存在"));
        organizationAccessService.assertCanAccess(tag);
        organizationAccessService.assertCanAccess(product);
        if (!tag.getStore().getId().equals(product.getStore().getId())) {
            throw new EruptWebApiRuntimeException("电子价签只能绑定同店铺商品");
        }
        tag.setProduct(product);
        EslTagRecord saved = tagRepository.save(tag);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "labelId", saved.getId(),
                "tagId", saved.getTagId(),
                "productId", product.getId(),
                "productName", product.getName()
        ));
    }

    private void rememberPreparedCommand(EslTagRecord tag, MqttCommand command) {
        tag.setLastTaskId(mqttCommandService.extractTaskId(command));
        tag.setLastPreparedAt(LocalDateTime.now());
        tag.setLastUpdatePayload(mqttCommandService.toJson(command.payload()));
        tagRepository.save(tag);
    }
}
