package com.aiden.einklabel.admin.template;

import com.aiden.einklabel.admin.template.operation.OpenTemplateEditorOperation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

@Entity
@Table(name = "esl_template")
@Erupt(
        name = "模板管理",
        power = @Power(export = true),
        orderBy = "TemplateRecord.updateTime desc",
        rowOperation = @RowOperation(
                code = "open_editor",
                title = "编辑模板",
                icon = "fa fa-pencil",
                mode = RowOperation.Mode.SINGLE,
                callHint = "",
                operationHandler = OpenTemplateEditorOperation.class
        )
)
public class TemplateRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EruptField(
            views = @View(title = "模板ID", sortable = true, width = "90px"),
            edit = @Edit(title = "模板ID", readonly = @Readonly(add = true, edit = true))
    )
    private Long id;

    @Column(nullable = false, length = 120)
    @EruptField(
            views = @View(title = "模板名称", sortable = true),
            edit = @Edit(title = "模板名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @Column(nullable = false, length = 12)
    @EruptField(
            views = @View(title = "色彩模式", sortable = true, width = "110px"),
            edit = @Edit(
                    title = "色彩模式",
                    notNull = true,
                    search = @Search,
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            type = ChoiceType.Type.RADIO,
                            vl = {
                                    @VL(value = "BW", label = "黑白"),
                                    @VL(value = "BWR", label = "黑白红"),
                                    @VL(value = "BWRY", label = "黑白红黄"),
                                    @VL(value = "E6", label = "六色")
                            }
                    )
            )
    )
    private String colorMode = ColorMode.BWR.name();

    @Column(nullable = false)
    @EruptField(
            views = @View(title = "宽度", sortable = true, width = "90px"),
            edit = @Edit(
                    title = "宽度",
                    notNull = true,
                    type = EditType.NUMBER,
                    numberType = @NumberType(min = 1, max = 4096)
            )
    )
    private Integer width = 296;

    @Column(nullable = false)
    @EruptField(
            views = @View(title = "高度", sortable = true, width = "90px"),
            edit = @Edit(
                    title = "高度",
                    notNull = true,
                    type = EditType.NUMBER,
                    numberType = @NumberType(min = 1, max = 4096)
            )
    )
    private Integer height = 128;

    @Lob
    @Column(name = "full_json")
    @EruptField(
            views = @View(title = "Full JSON", show = false),
            edit = @Edit(title = "Full JSON", show = false, type = EditType.TEXTAREA)
    )
    private String fullJson;

    @Lob
    @Column(name = "static_dynamic")
    @EruptField(
            views = @View(title = "Static Dynamic", show = false),
            edit = @Edit(title = "Static Dynamic", show = false, type = EditType.TEXTAREA)
    )
    private String staticDynamic;

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

    private void applyDefaults() {
        if (this.colorMode == null) {
            this.colorMode = ColorMode.BWR.name();
        }
        if (this.width == null) {
            this.width = 296;
        }
        if (this.height == null) {
            this.height = 128;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorMode() {
        return colorMode;
    }

    public void setColorMode(String colorMode) {
        this.colorMode = colorMode;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getFullJson() {
        return fullJson;
    }

    public void setFullJson(String fullJson) {
        this.fullJson = fullJson;
    }

    public String getStaticDynamic() {
        return staticDynamic;
    }

    public void setStaticDynamic(String staticDynamic) {
        this.staticDynamic = staticDynamic;
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
