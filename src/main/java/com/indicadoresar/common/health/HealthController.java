package com.indicadoresar.common.health;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        return buildHealthResponse();
    }

    private ResponseEntity<Map<String, String>> buildHealthResponse() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
