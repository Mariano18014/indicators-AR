package com.indicadoresar.dashboard;

import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.indicators.IndicatorResponse;
import com.indicadoresar.indicators.IndicatorService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/indicators/compare")
public class IndicatorCompareController {

    private final IndicatorService indicatorService;
    private final IndicatorRepository indicatorRepository;

    public IndicatorCompareController(
            IndicatorService indicatorService, IndicatorRepository indicatorRepository) {
        this.indicatorService = indicatorService;
        this.indicatorRepository = indicatorRepository;
    }

    @GetMapping
    public String showCompare(
            @RequestParam(required = false) String code1,
            @RequestParam(required = false) String code2,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {
        List<IndicatorResponse> indicators = findAllIndicators();
        String[] resolvedCodes = resolveCodes(indicators, code1, code2);
        LocalDate[] resolvedRange = resolveRange(resolvedCodes, from, to);
        validateCodes(resolvedCodes[0], resolvedCodes[1]);
        addAttributesToModel(model, indicators, resolvedCodes[0], resolvedCodes[1], resolvedRange[0], resolvedRange[1]);
        return "indicator/compare";
    }

    private List<IndicatorResponse> findAllIndicators() {
        return indicatorService.findAllIndicators();
    }

    private String[] resolveCodes(List<IndicatorResponse> indicators, String code1, String code2) {
        String resolvedCode1 = resolveCode(indicators, code1, 0);
        String resolvedCode2 = resolveCode(indicators, code2, 1);
        return new String[] {resolvedCode1, resolvedCode2};
    }

    private String resolveCode(List<IndicatorResponse> indicators, String code, int index) {
        if (code != null && !code.isBlank()) {
            return code;
        }
        if (indicators.size() > index) {
            return indicators.get(index).code();
        }
        return indicators.isEmpty() ? null : indicators.get(0).code();
    }

    private LocalDate[] resolveRange(String[] codes, LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = resolveFrom(codes, from);
        LocalDate resolvedTo = resolveTo(to);
        return new LocalDate[] {resolvedFrom, resolvedTo};
    }

    private LocalDate resolveFrom(String[] codes, LocalDate from) {
        if (from != null) {
            return from;
        }
        return buildDefaultFrom(codes);
    }

    private LocalDate buildDefaultFrom(String[] codes) {
        boolean hasMonthly = hasMonthlyFrequency(codes);
        LocalDate now = LocalDate.now();
        if (hasMonthly) {
            return now.minusMonths(12);
        }
        return now.minusDays(90);
    }

    private boolean hasMonthlyFrequency(String[] codes) {
        for (String code : codes) {
            if (code == null) continue;
            Indicator indicator = findIndicatorByCode(code);
            if (indicator != null && "MENSUAL".equals(indicator.getFrequency().name())) {
                return true;
            }
        }
        return false;
    }

    private Indicator findIndicatorByCode(String code) {
        return indicatorRepository.findByCode(code).orElse(null);
    }

    private LocalDate resolveTo(LocalDate to) {
        if (to != null) {
            return to;
        }
        return LocalDate.now();
    }

    private void validateCodes(String code1, String code2) {
        if (code1 != null && code1.equals(code2)) {
            throw new IllegalArgumentException("indicators must be different");
        }
    }

    private void addAttributesToModel(
            Model model,
            List<IndicatorResponse> indicators,
            String code1,
            String code2,
            LocalDate from,
            LocalDate to) {
        model.addAttribute("indicators", indicators);
        model.addAttribute("code1", code1);
        model.addAttribute("code2", code2);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
    }
}
