package com.aiden.einklabel.admin.esl;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.aiden.einklabel.admin.esl.operation.OpenEslTagUpdateCommandOperation;
import com.aiden.einklabel.admin.org.OrganizationScopedDataProxy;
import com.aiden.einklabel.admin.org.OrganizationScopedRecord;
import com.aiden.einklabel.admin.product.ProductRecord;
import com.aiden.einklabel.admin.store.StoreRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

@Entity
@Table(
        name = "esl_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_esl_tag_org_tag_id", columnNames = {"organization_id", "tag_id"})
)
@Erupt(
        name = "电子价签管理",
        power = @Power(export = true),
        orderBy = "EslTagRecord.updateTime desc",
        dataProxy = OrganizationScopedDataProxy.class,
        rowOperation = @RowOperation(
                code = "open_label_update_command",
                title = "商品下发数据",
                icon = "fa fa-paper-plane",
                mode = RowOperation.Mode.SINGLE,
                operationHandler = OpenEslTagUpdateCommandOperation.class
        )
)
public class EslTagRecord extends OrganizationScopedRecord {

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @EruptField(
            views = @View(title = "店铺", column = "name", sortable = true, width = "140px"),
            edit = @Edit(
                    title = "所属店铺",
                    notNull = true,
                    search = @Search,
                    type = EditType.REFERENCE_TABLE,
                    referenceTableType = @ReferenceTableType(label = "name")
            )
    )
    private StoreRecord store;

    @ManyToOne
    @JoinColumn(name = "access_point_id")
    @EruptField(
            views = @View(title = "AP", column = "sn", sortable = true, width = "150px"),
            edit = @Edit(
                    title = "关联AP",
                    search = @Search,
                    type = EditType.REFERENCE_TABLE,
                    referenceTableType = @ReferenceTableType(label = "sn")
            )
    )
    private AccessPointRecord accessPoint;

    @Column(name = "tag_id", nullable = false)
    @EruptField(
            views = @View(title = "价签ID", sortable = true, width = "150px"),
            edit = @Edit(
                    title = "价签ID（十进制）",
                    notNull = true,
                    search = @Search,
                    type = EditType.NUMBER,
                    numberType = @NumberType(min = 1, max = Long.MAX_VALUE)
            )
    )
    private Long tagId;

    @Column(nullable = false)
    @EruptField(
            views = @View(title = "型号", sortable = true, width = "90px"),
            edit = @Edit(
                    title = "价签型号",
                    notNull = true,
                    type = EditType.NUMBER,
                    numberType = @NumberType(min = 1, max = 999999)
            )
    )
    private Integer model = 6;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @EruptField(
            views = @View(title = "绑定商品", column = "name", sortable = true, width = "180px"),
            edit = @Edit(
                    title = "绑定商品",
                    search = @Search,
                    type = EditType.REFERENCE_TABLE,
                    referenceTableType = @ReferenceTableType(label = "name")
            )
    )
    private ProductRecord product;

    @Column(name = "force_refresh", nullable = false)
    @EruptField(
            views = @View(title = "强制刷新", width = "100px"),
            edit = @Edit(
                    title = "强制刷新",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "更新数据刷新", falseText = "仅刷新屏幕")
            )
    )
    private Boolean forceRefresh = true;

    @Column(name = "protocol_status")
    @EruptField(
            views = @View(title = "协议状态", sortable = true, width = "100px"),
            edit = @Edit(
                    title = "协议状态（1-8）",
                    type = EditType.NUMBER,
                    numberType = @NumberType(min = 1, max = 8)
            )
    )
    private Integer protocolStatus;

    @Column(name = "battery_soc")
    @EruptField(
            views = @View(title = "电量", width = "90px"),
            edit = @Edit(
                    title = "电量",
                    type = EditType.NUMBER,
                    numberType = @NumberType(min = 0, max = 100)
            )
    )
    private Integer batterySoc;

    @Column(name = "rssi")
    @EruptField(
            views = @View(title = "信号", width = "90px"),
            edit = @Edit(title = "信号值", type = EditType.NUMBER)
    )
    private Integer rssi;

    @Column(name = "temperature")
    @EruptField(
            views = @View(title = "温度", show = false),
            edit = @Edit(title = "温度", type = EditType.NUMBER)
    )
    private Integer temperature;

    @Column(name = "last_task_id")
    @EruptField(
            views = @View(title = "任务ID", show = false),
            edit = @Edit(title = "最近任务ID", type = EditType.NUMBER)
    )
    private Integer lastTaskId;

    @Column(name = "token")
    @EruptField(
            views = @View(title = "Token", show = false),
            edit = @Edit(title = "Token", type = EditType.NUMBER)
    )
    private Integer token;

    @EruptField(
            views = @View(title = "最后生成", sortable = true, width = "160px"),
            edit = @Edit(title = "最后生成下发数据时间")
    )
    private LocalDateTime lastPreparedAt;

    @Lob
    @Column(name = "last_update_payload")
    @EruptField(
            views = @View(title = "最近下发数据", show = false),
            edit = @Edit(title = "最近下发数据", show = false, type = EditType.TEXTAREA)
    )
    private String lastUpdatePayload;

    @Override
    protected void applyDefaults() {
        if (this.model == null) {
            this.model = 6;
        }
        if (this.forceRefresh == null) {
            this.forceRefresh = true;
        }
    }

    public StoreRecord getStore() {
        return store;
    }

    public void setStore(StoreRecord store) {
        this.store = store;
    }

    public AccessPointRecord getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(AccessPointRecord accessPoint) {
        this.accessPoint = accessPoint;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Integer getModel() {
        return model;
    }

    public void setModel(Integer model) {
        this.model = model;
    }

    public ProductRecord getProduct() {
        return product;
    }

    public void setProduct(ProductRecord product) {
        this.product = product;
    }

    public Boolean getForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(Boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    public Integer getProtocolStatus() {
        return protocolStatus;
    }

    public void setProtocolStatus(Integer protocolStatus) {
        this.protocolStatus = protocolStatus;
    }

    public Integer getBatterySoc() {
        return batterySoc;
    }

    public void setBatterySoc(Integer batterySoc) {
        this.batterySoc = batterySoc;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Integer getLastTaskId() {
        return lastTaskId;
    }

    public void setLastTaskId(Integer lastTaskId) {
        this.lastTaskId = lastTaskId;
    }

    public Integer getToken() {
        return token;
    }

    public void setToken(Integer token) {
        this.token = token;
    }

    public LocalDateTime getLastPreparedAt() {
        return lastPreparedAt;
    }

    public void setLastPreparedAt(LocalDateTime lastPreparedAt) {
        this.lastPreparedAt = lastPreparedAt;
    }

    public String getLastUpdatePayload() {
        return lastUpdatePayload;
    }

    public void setLastUpdatePayload(String lastUpdatePayload) {
        this.lastUpdatePayload = lastUpdatePayload;
    }
}
