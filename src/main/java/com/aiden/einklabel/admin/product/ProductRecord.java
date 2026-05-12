package com.aiden.einklabel.admin.product;

import com.aiden.einklabel.admin.org.OrganizationScopedDataProxy;
import com.aiden.einklabel.admin.org.OrganizationScopedRecord;
import com.aiden.einklabel.admin.store.StoreRecord;
import com.aiden.einklabel.admin.template.TemplateRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

@Entity
@Table(
        name = "esl_product",
        uniqueConstraints = @UniqueConstraint(name = "uk_esl_product_org_store_code", columnNames = {"organization_id", "store_id", "code"})
)
@Erupt(
        name = "商品管理",
        power = @Power(export = true),
        orderBy = "ProductRecord.updateTime desc",
        dataProxy = OrganizationScopedDataProxy.class
)
public class ProductRecord extends OrganizationScopedRecord {

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @EruptField(
            views = @View(title = "店铺", column = "name", sortable = true, width = "140px"),
            edit = @Edit(
                    title = "店铺",
                    notNull = true,
                    search = @Search,
                    type = EditType.REFERENCE_TABLE,
                    referenceTableType = @ReferenceTableType(label = "name")
            )
    )
    private StoreRecord store;

    @Column(nullable = false, length = 80)
    @EruptField(
            views = @View(title = "商品编码", sortable = true, width = "140px"),
            edit = @Edit(title = "商品编码", notNull = true, search = @Search(vague = true))
    )
    private String code;

    @Column(nullable = false, length = 180)
    @EruptField(
            views = @View(title = "商品名称", sortable = true),
            edit = @Edit(title = "商品名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @Column(length = 180)
    @EruptField(
            views = @View(title = "商品全称", show = false),
            edit = @Edit(title = "商品全称", search = @Search(vague = true))
    )
    private String fullName;

    @Column(length = 80)
    @EruptField(
            views = @View(title = "品牌", width = "120px"),
            edit = @Edit(title = "品牌", search = @Search(vague = true))
    )
    private String brand;

    @Column(length = 80)
    @EruptField(
            views = @View(title = "规格", width = "120px"),
            edit = @Edit(title = "规格")
    )
    private String spec;

    @Column(precision = 12, scale = 2)
    @EruptField(
            views = @View(title = "价格", sortable = true, width = "100px"),
            edit = @Edit(title = "价格", type = EditType.NUMBER, numberType = @NumberType(min = 0, max = 999999999))
    )
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    @EruptField(
            views = @View(title = "促销价", sortable = true, width = "100px"),
            edit = @Edit(title = "促销价", type = EditType.NUMBER, numberType = @NumberType(min = 0, max = 999999999))
    )
    private BigDecimal promoPrice;

    @Column(length = 120)
    @EruptField(
            views = @View(title = "产地", show = false),
            edit = @Edit(title = "产地")
    )
    private String origin;

    @Column(length = 255)
    @EruptField(
            views = @View(title = "二维码", show = false),
            edit = @Edit(title = "二维码/链接")
    )
    private String qrCode;

    @Column(length = 80)
    @EruptField(
            views = @View(title = "开始时间", show = false),
            edit = @Edit(title = "促销开始时间")
    )
    private String promoStart;

    @Column(length = 80)
    @EruptField(
            views = @View(title = "结束时间", show = false),
            edit = @Edit(title = "促销结束时间")
    )
    private String promoEnd;

    @Column(length = 80)
    @EruptField(
            views = @View(title = "质检员", show = false),
            edit = @Edit(title = "质检员")
    )
    private String qualityInspector;

    @Column(length = 120)
    @EruptField(
            views = @View(title = "扩展F7", show = false),
            edit = @Edit(title = "扩展字段 F_7")
    )
    private String field7;

    @ManyToOne
    @JoinColumn(name = "template_id")
    @EruptField(
            views = @View(title = "绑定模板", column = "name", sortable = true, width = "160px"),
            edit = @Edit(
                    title = "绑定已有模板",
                    notNull = true,
                    search = @Search,
                    type = EditType.REFERENCE_TABLE,
                    referenceTableType = @ReferenceTableType(label = "name")
            )
    )
    private TemplateRecord template;

    @Column(length = 500)
    @EruptField(
            views = @View(title = "备注", show = false),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

    public StoreRecord getStore() {
        return store;
    }

    public void setStore(StoreRecord store) {
        this.store = store;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promoPrice) {
        this.promoPrice = promoPrice;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getPromoStart() {
        return promoStart;
    }

    public void setPromoStart(String promoStart) {
        this.promoStart = promoStart;
    }

    public String getPromoEnd() {
        return promoEnd;
    }

    public void setPromoEnd(String promoEnd) {
        this.promoEnd = promoEnd;
    }

    public String getQualityInspector() {
        return qualityInspector;
    }

    public void setQualityInspector(String qualityInspector) {
        this.qualityInspector = qualityInspector;
    }

    public String getField7() {
        return field7;
    }

    public void setField7(String field7) {
        this.field7 = field7;
    }

    public TemplateRecord getTemplate() {
        return template;
    }

    public void setTemplate(TemplateRecord template) {
        this.template = template;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
