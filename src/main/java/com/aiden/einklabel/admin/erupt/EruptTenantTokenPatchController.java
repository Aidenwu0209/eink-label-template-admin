package com.aiden.einklabel.admin.erupt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EruptTenantTokenPatchController {

    private static final String ERUPT_CHUNK_PATH = "public/chunk-T2V5GP5Q.js";
    private static final String UNSAFE_TENANT_TOKEN_CHECK = "this.tokenService.get().token.split(\".\").length==3";
    private static final String SAFE_TENANT_TOKEN_CHECK = "(this.tokenService.get().token||\"\").split(\".\").length==3";

    private final String patchedChunk;

    public EruptTenantTokenPatchController() {
        this.patchedChunk = loadPatchedChunk();
    }

    @GetMapping(value = "/chunk-T2V5GP5Q.js")
    @ResponseBody
    public ResponseEntity<String> chunk() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.valueOf("application/javascript"))
                .body(patchedChunk);
    }

    String patchedChunk() {
        return patchedChunk;
    }

    private String loadPatchedChunk() {
        try {
            ClassPathResource resource = new ClassPathResource(ERUPT_CHUNK_PATH);
            String chunk = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            if (!chunk.contains(UNSAFE_TENANT_TOKEN_CHECK)) {
                throw new IllegalStateException("Erupt web chunk no longer contains the expected tenant-token expression");
            }
            return chunk.replace(UNSAFE_TENANT_TOKEN_CHECK, SAFE_TENANT_TOKEN_CHECK);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load Erupt web chunk " + ERUPT_CHUNK_PATH, e);
        }
    }
}
