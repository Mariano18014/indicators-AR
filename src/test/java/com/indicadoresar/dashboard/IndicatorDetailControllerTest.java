package com.indicadoresar.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.support.PostgresContainerSupport;
import com.indicadoresar.values.IndicatorValueRepository;
import com.indicadoresar.values.IndicatorValueService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class IndicatorDetailControllerTest extends PostgresContainerSupport {

    @Autowired
    private IndicatorDetailController detailController;

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Autowired
    private IndicatorValueRepository indicatorValueRepository;

    @Autowired
    private IndicatorValueService indicatorValueService;

    @BeforeEach
    void setUp() {
        indicatorValueRepository.deleteAll();
    }

    @Test
    void showDetailReturnsDetailView() {
        seedHistory("DOLAR_OFICIAL");
        Model model = new ConcurrentModel();

        String view = detailController.showDetail("DOLAR_OFICIAL", null, null, model);

        assertThat(view).isEqualTo("indicator/detail");
    }

    @Test
    void showDetailAddsIndicatorAndHistoryToModel() {
        seedHistory("DOLAR_OFICIAL");
        Model model = new ConcurrentModel();

        detailController.showDetail("DOLAR_OFICIAL", null, null, model);

        assertThat(model.containsAttribute("indicator")).isTrue();
        assertThat(model.containsAttribute("history")).isTrue();
        var history = (java.util.List<?>) model.getAttribute("history");
        assertThat(history).isNotEmpty();
    }

    @Test
    void showDetailWithNoDataReturnsEmptyHistory() {
        Model model = new ConcurrentModel();

        detailController.showDetail("DOLAR_OFICIAL", null, null, model);

        var history = (java.util.List<?>) model.getAttribute("history");
        assertThat(history).isEmpty();
    }

    @Test
    void showDetailWithExplicitRangeFiltersHistory() {
        seedHistory("DOLAR_OFICIAL");
        Model model = new ConcurrentModel();
        LocalDate from = LocalDate.of(2026, 9, 6);
        LocalDate to = LocalDate.of(2026, 9, 6);

        detailController.showDetail("DOLAR_OFICIAL", from, to, model);

        var history = (java.util.List<?>) model.getAttribute("history");
        assertThat(history).hasSize(1);
    }

    @Test
    void showDetailThrows404ForUnknownCode() {
        Model model = new ConcurrentModel();

        org.junit.jupiter.api.Assertions.assertThrows(
                com.indicadoresar.common.exception.ResourceNotFoundException.class,
                () -> detailController.showDetail("UNKNOWN", null, null, model));
    }

    private void seedHistory(String code) {
        var indicator = indicatorRepository.findByCode(code).orElseThrow();
        indicatorValueService.saveOrUpdate(indicator, LocalDate.of(2026, 9, 5), new BigDecimal("1200.00"));
        indicatorValueService.saveOrUpdate(indicator, LocalDate.of(2026, 9, 6), new BigDecimal("1210.00"));
        indicatorValueService.saveOrUpdate(indicator, LocalDate.of(2026, 9, 7), new BigDecimal("1220.00"));
    }
}
