package com.aiden.einklabel.admin.template.api;

import com.aiden.einklabel.admin.template.ColorMode;
import com.aiden.einklabel.admin.template.TemplateEditorLinkService;
import com.aiden.einklabel.admin.template.TemplateRecord;
import com.aiden.einklabel.admin.template.TemplateRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TemplateController {

    private final TemplateRecordRepository repository;

    private final TemplateEditorLinkService linkService;

    private final ObjectMapper objectMapper;

    public TemplateController(
            TemplateRecordRepository repository,
            TemplateEditorLinkService linkService,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.linkService = linkService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<TemplateResponse> getTemplate(@PathVariable Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/templates/{id}/editor-url")
    public ResponseEntity<Map<String, String>> getEditorUrl(@PathVariable Long id) {
        return repository.findById(id)
                .map(template -> Map.of("url", linkService.buildEditorUrl(template)))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/template/save")
    @Transactional
    public TemplateSaveResponse saveTemplate(@RequestBody JsonNode payload) {
        TemplateRecord template = resolveTemplate(payload);
        template.setName(readText(payload, "templateName", template.getName(), "电子价签模板"));

        JsonNode profile = payload.path("profile");
        template.setWidth(readInt(profile, "width", template.getWidth(), 296));
        template.setHeight(readInt(profile, "height", template.getHeight(), 128));
        template.setColorMode(readColorMode(
                profile.path("colorMode").asText(null),
                readColorMode(template.getColorMode(), ColorMode.BWR)
        ).name());

        JsonNode fullJson = payload.path("fullJson");
        if (!fullJson.isMissingNode() && !fullJson.isNull()) {
            template.setFullJson(toJson(fullJson));
        }
        JsonNode staticDynamic = payload.path("staticDynamic");
        if (!staticDynamic.isMissingNode() && !staticDynamic.isNull()) {
            template.setStaticDynamic(toJson(staticDynamic));
        }

        TemplateRecord saved = repository.save(template);
        return new TemplateSaveResponse(true, saved.getId().toString(), linkService.buildEditorUrl(saved));
    }

    private TemplateRecord resolveTemplate(JsonNode payload) {
        Long id = parseLong(payload.path("templateId").asText(null));
        if (id != null) {
            return repository.findById(id).orElseGet(() -> {
                TemplateRecord template = new TemplateRecord();
                template.setId(id);
                return template;
            });
        }
        return new TemplateRecord();
    }

    private TemplateResponse toResponse(TemplateRecord template) {
        TemplateResponse.Meta meta = new TemplateResponse.Meta(
                screenTypeFor(readColorMode(template.getColorMode(), ColorMode.BWR)),
                readColorMode(template.getColorMode(), ColorMode.BWR).name(),
                template.getWidth(),
                template.getHeight()
        );
        return new TemplateResponse(
                template.getId().toString(),
                template.getName(),
                meta,
                readJson(template.getFullJson(), "{\"objects\":[]}")
        );
    }

    private JsonNode readJson(String json, String fallback) {
        try {
            return objectMapper.readTree((json == null || json.isBlank()) ? fallback : json);
        } catch (Exception ignored) {
            try {
                return objectMapper.readTree(fallback);
            } catch (Exception e) {
                throw new IllegalStateException("Invalid fallback JSON", e);
            }
        }
    }

    private String toJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON payload", e);
        }
    }

    private String readText(JsonNode node, String field, String current, String fallback) {
        String value = node.path(field).asText(null);
        if (value != null && !value.isBlank()) {
            return value;
        }
        if (current != null && !current.isBlank()) {
            return current;
        }
        return fallback;
    }

    private Integer readInt(JsonNode node, String field, Integer current, int fallback) {
        JsonNode value = node.path(field);
        if (value.isInt() && value.asInt() > 0) {
            return value.asInt();
        }
        if (value.isTextual()) {
            try {
                int parsed = Integer.parseInt(value.asText());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return current != null ? current : fallback;
    }

    private ColorMode readColorMode(String value, ColorMode current) {
        if (value != null) {
            try {
                return ColorMode.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return current != null ? current : ColorMode.BWR;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String screenTypeFor(ColorMode colorMode) {
        return switch (colorMode) {
            case BW -> "bw";
            case BWR -> "tri";
            case BWRY -> "bwry";
            case E6 -> "six";
        };
    }

    public record TemplateResponse(String id, String name, Meta meta, JsonNode fabricJson) {
        public record Meta(String screenType, String colorMode, Integer width, Integer height) {
        }
    }

    public record TemplateSaveResponse(boolean success, String templateId, String editorUrl) {
    }
}
