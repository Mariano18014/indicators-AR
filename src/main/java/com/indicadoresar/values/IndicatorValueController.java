package com.indicadoresar.values;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indicators")
public class IndicatorValueController {

    private final IndicatorValueService indicatorValueService;

    public IndicatorValueController(IndicatorValueService indicatorValueService) {
        this.indicatorValueService = indicatorValueService;
    }

    @GetMapping("/{code}/history")
    public ResponseEntity<List<IndicatorValueResponse>> getHistory(
            @PathVariable String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<IndicatorValueResponse> history = findHistory(code, from, to);
        return ResponseEntity.ok(history);
    }

    private List<IndicatorValueResponse> findHistory(String code, LocalDate from, LocalDate to) {
        return indicatorValueService.findHistory(code, from, to);
    }
}
