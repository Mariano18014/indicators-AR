package com.indicadoresar.indicators;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indicators")
public class IndicatorController {

    private final IndicatorService indicatorService;

    public IndicatorController(IndicatorService indicatorService) {
        this.indicatorService = indicatorService;
    }

    @GetMapping
    public ResponseEntity<List<IndicatorResponse>> getIndicators() {
        List<IndicatorResponse> indicators = findAllIndicators();
        return ResponseEntity.ok(indicators);
    }

    private List<IndicatorResponse> findAllIndicators() {
        return indicatorService.findAllIndicators();
    }
}
