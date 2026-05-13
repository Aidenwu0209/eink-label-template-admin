package com.aiden.einklabel.admin;

import com.aiden.einklabel.admin.config.AdminDemoDataProperties;
import com.aiden.einklabel.admin.config.TemplateEditorProperties;
import com.aiden.einklabel.admin.config.TaskProducerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import xyz.erupt.core.annotation.EruptScan;

@SpringBootApplication
@EruptScan("com.aiden.einklabel.admin")
@EntityScan(basePackages = {"com.aiden.einklabel.admin", "xyz.erupt"})
@EnableConfigurationProperties({TemplateEditorProperties.class, TaskProducerProperties.class, AdminDemoDataProperties.class})
public class EinkLabelTemplateAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(EinkLabelTemplateAdminApplication.class, args);
    }
}
