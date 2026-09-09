package com.indicadoresar.dashboard;

import com.indicadoresar.common.exception.ResourceNotFoundException;
import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorFrequency;
import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.values.IndicatorValueResponse;
import com.indicadoresar.values.IndicatorValueService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/indicators")
public class IndicatorDetailController {

    private final IndicatorRepository indicatorRepository;
    private final IndicatorValueService indicatorValueService;

    public IndicatorDetailController(
            IndicatorRepository indicatorRepository, IndicatorValueService indicatorValueService) {
        this.indicatorRepository = indicatorRepository;
        this.indicatorValueService = indicatorValueService;
    }

    @GetMapping("/{code}")
    public String showDetail(
            @PathVariable String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {
        Indicator indicator = findIndicatorByCode(code);
        LocalDate[] range = resolveRange(indicator, from, to);
        List<IndicatorValueResponse> history = findHistory(code, range[0], range[1]);
        addAttributesToModel(model, indicator, history, range[0], range[1]);
        return "indicator/detail";
    }

    private Indicator findIndicatorByCode(String code) {
        return indicatorRepository
                .findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found: " + code));
    }

    private LocalDate[] resolveRange(Indicator indicator, LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = resolveFrom(indicator, from);
        LocalDate resolvedTo = resolveTo(to);
        return new LocalDate[] {resolvedFrom, resolvedTo};
    }

    private LocalDate resolveFrom(Indicator indicator, LocalDate from) {
        if (from != null) {
            return from;
        }
        return buildDefaultFrom(indicator);
    }

    private LocalDate buildDefaultFrom(Indicator indicator) {
        LocalDate now = LocalDate.now();
        if (indicator.getFrequency() == IndicatorFrequency.MENSUAL) {
            return now.minusMonths(12);
        }
        return now.minusDays(90);
    }

    private LocalDate resolveTo(LocalDate to) {
        if (to != null) {
            return to;
        }
        return LocalDate.now();
    }

    private List<IndicatorValueResponse> findHistory(String code, LocalDate from, LocalDate to) {
        return indicatorValueService.findHistory(code, from, to);
    }

    private void addAttributesToModel(
            Model model, Indicator indicator, List<IndicatorValueResponse> history, LocalDate from, LocalDate to) {
        model.addAttribute("indicator", indicator);
        model.addAttribute("history", history);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
    }
}
