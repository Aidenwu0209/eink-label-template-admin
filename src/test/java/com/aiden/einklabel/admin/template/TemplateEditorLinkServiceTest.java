package com.aiden.einklabel.admin.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aiden.einklabel.admin.config.TemplateEditorProperties;
import com.aiden.einklabel.admin.template.api.TemplateController;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TemplateEditorLinkServiceTest {

    @Test
    void buildsEditorUrlWithTemplateMetadataAndBackendEndpoints() {
        TemplateEditorProperties properties = new TemplateEditorProperties();
        properties.setBaseUrl("http://127.0.0.1:5173/");
        properties.setApiBaseUrl("http://127.0.0.1:8080/api");
        properties.setSaveApiUrl("http://127.0.0.1:8080/api/template/save");

        TemplateRecord template = new TemplateRecord();
        template.setId(12L);
        template.setName("门店黑白红模板");
        template.setColorMode(ColorMode.BWR.name());
        template.setWidth(296);
        template.setHeight(128);

        String url = new TemplateEditorLinkService(properties).buildEditorUrl(template);

        assertThat(url).startsWith("http://127.0.0.1:5173/?");
        assertThat(url).contains("templateId=12");
        assertThat(url).contains("templateName=%E9%97%A8%E5%BA%97%E9%BB%91%E7%99%BD%E7%BA%A2%E6%A8%A1%E6%9D%BF");
        assertThat(url).contains("width=296");
        assertThat(url).contains("height=128");
        assertThat(url).contains("colorMode=BWR");
        assertThat(url).contains("apiBase=http://127.0.0.1:8080/api");
        assertThat(url).contains("saveApi=http://127.0.0.1:8080/api/template/save");
    }

    @Test
    void saveCreatesNewTemplateWhenClientTemplateIdDoesNotExist() throws Exception {
        TemplateRecordRepository repository = mock(TemplateRecordRepository.class);
        TemplateRecord saved = new TemplateRecord();
        saved.setId(99L);
        saved.setName("新模板");
        saved.setWidth(296);
        saved.setHeight(128);
        saved.setColorMode("BWR");
        when(repository.findById(9101L)).thenReturn(Optional.empty());
        when(repository.save(any(TemplateRecord.class))).thenReturn(saved);

        TemplateController controller = new TemplateController(
                repository,
                new TemplateEditorLinkService(properties()),
                new ObjectMapper()
        );

        TemplateController.TemplateSaveResponse response = controller.saveTemplate(new ObjectMapper().readTree("""
                {
                  "templateId": "9101",
                  "templateName": "新模板",
                  "profile": {"width": 296, "height": 128, "colorMode": "BWR"},
                  "fullJson": {"objects": []}
                }
                """));

        assertThat(response.success()).isTrue();
        assertThat(response.templateId()).isEqualTo("99");
        verify(repository).save(any(TemplateRecord.class));
    }

    private TemplateEditorProperties properties() {
        TemplateEditorProperties properties = new TemplateEditorProperties();
        properties.setBaseUrl("http://127.0.0.1:5173/");
        properties.setApiBaseUrl("http://127.0.0.1:8080/api");
        properties.setSaveApiUrl("http://127.0.0.1:8080/api/template/save");
        return properties;
    }
}
