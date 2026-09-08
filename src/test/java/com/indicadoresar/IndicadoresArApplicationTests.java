package com.indicadoresar;

import static org.assertj.core.api.Assertions.assertThat;

import com.indicadoresar.common.health.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

class IndicadoresArApplicationTests {

    @Test
    void healthControllerReturnsUp() {
        HealthController controller = new HealthController();

        ResponseEntity<Map<String, String>> response = controller.checkHealth();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsEntry("status", "UP");
    }

    @Test
    void applicationClassExists() {
        assertThat(IndicadoresArApplication.class).isNotNull();
    }
}
