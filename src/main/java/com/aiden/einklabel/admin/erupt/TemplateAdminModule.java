package com.aiden.einklabel.admin.erupt;

import com.aiden.einklabel.admin.template.TemplateRecord;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;

@Configuration
public class TemplateAdminModule implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(TemplateAdminModule.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder()
                .name("eink-template-admin")
                .description("E-ink label template management")
                .build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu root = MetaMenu.createRootMenu("$template", "模板中心", "fa fa-object-group", 2);
        menus.add(root);
        menus.add(MetaMenu.createEruptClassMenu(TemplateRecord.class, root, 10));
        return menus;
    }
}
