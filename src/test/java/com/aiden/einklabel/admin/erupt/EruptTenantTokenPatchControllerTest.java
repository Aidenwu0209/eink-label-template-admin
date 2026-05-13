package com.aiden.einklabel.admin.erupt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EruptTenantTokenPatchControllerTest {

    @Test
    void patchesTenantTokenCheckToHandleMissingToken() {
        EruptTenantTokenPatchController controller = new EruptTenantTokenPatchController();

        assertThat(controller.patchedChunk())
                .contains("(this.tokenService.get().token||\"\").split(\".\").length==3")
                .doesNotContain("this.tokenService.get().token.split(\".\").length==3");
    }
}
