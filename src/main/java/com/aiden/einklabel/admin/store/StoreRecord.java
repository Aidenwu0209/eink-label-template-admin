package com.aiden.einklabel.admin.store;

import com.aiden.einklabel.admin.org.OrganizationScopedDataProxy;
import com.aiden.einklabel.admin.org.OrganizationScopedRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

@Entity
@Table(
        name = "esl_store",
        uniqueConstraints = @UniqueConstraint(name = "uk_esl_store_org_code", columnNames = {"organization_id", "code"})
)
@Erupt(
        name = "店铺管理",
        power = @Power(export = true),
        orderBy = "StoreRecord.updateTime desc",
        dataProxy = OrganizationScopedDataProxy.class
)
public class StoreRecord extends OrganizationScopedRecord {

    @Column(nullable = false, length = 64)
    @EruptField(
            views = @View(title = "门店代码", sortable = true, width = "120px"),
            edit = @Edit(title = "门店代码", notNull = true, search = @Search(vague = true))
    )
    private String code;

    @Column(nullable = false, length = 120)
    @EruptField(
            views = @View(title = "门店名称", sortable = true),
            edit = @Edit(title = "门店名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @Column(name = "shop_id", nullable = false)
    @EruptField(
            views = @View(title = "门店ID", sortable = true, width = "100px"),
            edit = @Edit(
                    title = "门店ID",
                    notNull = true,
                    type = EditType.NUMBER,
                    numberType = @NumberType(min = 1, max = 999999999)
            )
    )
    private Integer shopId = 1;

    @Column(length = 255)
    @EruptField(
            views = @View(title = "地址", show = false),
            edit = @Edit(title = "地址", search = @Search(vague = true))
    )
    private String address;

    @Column(length = 64)
    @EruptField(
            views = @View(title = "负责人", show = false),
            edit = @Edit(title = "负责人")
    )
    private String managerName;

    @Column(length = 32)
    @EruptField(
            views = @View(title = "联系电话", show = false),
            edit = @Edit(title = "联系电话")
    )
    private String phone;

    @Column(nullable = false)
    @EruptField(
            views = @View(title = "状态", sortable = true, width = "90px"),
            edit = @Edit(
                    title = "状态",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(trueText = "启用", falseText = "停用")
            )
    )
    private Boolean active = true;

    @Column(length = 500)
    @EruptField(
            views = @View(title = "备注", show = false),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

    @Override
    protected void applyDefaults() {
        if (this.shopId == null) {
            this.shopId = 1;
        }
        if (this.active == null) {
            this.active = true;
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
