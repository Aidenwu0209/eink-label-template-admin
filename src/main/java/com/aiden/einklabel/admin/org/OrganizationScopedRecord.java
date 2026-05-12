package com.aiden.einklabel.admin.org;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.model.EruptOrg;

@MappedSuperclass
public abstract class OrganizationScopedRecord implements OrganizationScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EruptField(
            views = @View(title = "ID", sortable = true, width = "80px"),
            edit = @Edit(title = "ID", readonly = @Readonly(add = true, edit = true))
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @EruptField(
            views = @View(title = "组织", column = "name", sortable = true, width = "140px"),
            edit = @Edit(
                    title = "组织",
                    notNull = true,
                    search = @Search,
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentOrg.id")
            )
    )
    private EruptOrg organization;

    @EruptField(
            views = @View(title = "更新时间", sortable = true, width = "160px"),
            edit = @Edit(title = "更新时间", readonly = @Readonly(add = true, edit = true))
    )
    private LocalDateTime updateTime;

    @EruptField(
            views = @View(title = "创建时间", show = false),
            edit = @Edit(title = "创建时间", show = false)
    )
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        applyDefaults();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
        applyDefaults();
    }

    protected void applyDefaults() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public EruptOrg getOrganization() {
        return organization;
    }

    @Override
    public void setOrganization(EruptOrg organization) {
        this.organization = organization;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
