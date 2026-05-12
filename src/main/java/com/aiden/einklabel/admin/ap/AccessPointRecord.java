package com.aiden.einklabel.admin.ap;

import com.aiden.einklabel.admin.ap.operation.OpenAccessPointShopCommandOperation;
import com.aiden.einklabel.admin.org.OrganizationScopedDataProxy;
import com.aiden.einklabel.admin.org.OrganizationScopedRecord;
import com.aiden.einklabel.admin.store.StoreRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
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
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

@Entity
@Table(
        name = "esl_access_point",
        uniqueConstraints = @UniqueConstraint(name = "uk_esl_ap_org_sn", columnNames = {"organization_id", "sn"})
)
@Erupt(
        name = "AP管理",
        power = @Power(export = true),
        orderBy = "AccessPointRecord.updateTime desc",
        dataProxy = OrganizationScopedDataProxy.class,
        rowOperation = @RowOperation(
                code = "open_shop_command",
                title = "门店配置数据",
                icon = "fa fa-exchange",
                mode = RowOperation.Mode.SINGLE,
                operationHandler = OpenAccessPointShopCommandOperation.class
        )
)
public class AccessPointRecord extends OrganizationScopedRecord {

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @EruptField(
            views = @View(title = "店铺", column = "name", sortable = true, width = "140px"),
            edit = @Edit(
                    title = "绑定店铺",
                    notNull = true,
                    search = @Search,
                    type = EditType.REFERENCE_TABLE,
                    referenceTableType = @ReferenceTableType(label = "name")
            )
    )
    private StoreRecord store;

    @Column(nullable = false, length = 80)
    @EruptField(
            views = @View(title = "AP编码", sortable = true, width = "150px"),
            edit = @Edit(title = "AP编码/SN", notNull = true, search = @Search(vague = true))
    )
    private String sn;

    @Column(name = "shop_no", nullable = false)
    @EruptField(
            views = @View(title = "店内编号", sortable = true, width = "100px"),
            edit = @Edit(
                    title = "店内AP编号",
                    notNull = true,
                    type = EditType.NUMBER,
                    numberType = @NumberType(min = 1, max = 65535)
            )
    )
    private Integer shopNo = 1;

    @Column(length = 80)
    @EruptField(
            views = @View(title = "MQTT ClientId", show = false),
            edit = @Edit(title = "MQTT ClientId")
    )
    private String mqttClientId;

    @Column(length = 80)
    @EruptField(
            views = @View(title = "型号", show = false),
            edit = @Edit(title = "型号")
    )
    private String model;

    @Column(length = 80)
    @EruptField(
            views = @View(title = "固件版本", show = false),
            edit = @Edit(title = "固件版本")
    )
    private String firmwareVersion;

    @Column(length = 40)
    @EruptField(
            views = @View(title = "MAC", show = false),
            edit = @Edit(title = "MAC地址")
    )
    private String macAddress;

    @Column(length = 48)
    @EruptField(
            views = @View(title = "IP", width = "120px"),
            edit = @Edit(title = "IP地址", search = @Search(vague = true))
    )
    private String ipAddress;

    @Column(nullable = false, length = 24)
    @EruptField(
            views = @View(title = "状态", sortable = true, width = "100px"),
            edit = @Edit(
                    title = "状态",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            type = ChoiceType.Type.RADIO,
                            vl = {
                                    @VL(value = "OFFLINE", label = "离线"),
                                    @VL(value = "ONLINE", label = "在线"),
                                    @VL(value = "MAINTENANCE", label = "维护")
                            }
                    )
            )
    )
    private String status = "OFFLINE";

    @EruptField(
            views = @View(title = "最后心跳", sortable = true, width = "160px"),
            edit = @Edit(title = "最后心跳")
    )
    private LocalDateTime lastHeartbeatAt;

    @Column(length = 40)
    @EruptField(
            views = @View(title = "CPU", show = false),
            edit = @Edit(title = "CPU使用率")
    )
    private String cpuUsage;

    @Column(length = 40)
    @EruptField(
            views = @View(title = "内存", show = false),
            edit = @Edit(title = "内存使用率")
    )
    private String memoryUsage;

    @Column(length = 40)
    @EruptField(
            views = @View(title = "磁盘", show = false),
            edit = @Edit(title = "磁盘使用率")
    )
    private String diskUsage;

    @Column(length = 500)
    @EruptField(
            views = @View(title = "备注", show = false),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

    @Override
    protected void applyDefaults() {
        if (this.shopNo == null) {
            this.shopNo = 1;
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "OFFLINE";
        }
    }

    public StoreRecord getStore() {
        return store;
    }

    public void setStore(StoreRecord store) {
        this.store = store;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public Integer getShopNo() {
        return shopNo;
    }

    public void setShopNo(Integer shopNo) {
        this.shopNo = shopNo;
    }

    public String getMqttClientId() {
        return mqttClientId;
    }

    public void setMqttClientId(String mqttClientId) {
        this.mqttClientId = mqttClientId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(LocalDateTime lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }

    public String getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(String cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public String getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(String memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public String getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(String diskUsage) {
        this.diskUsage = diskUsage;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
