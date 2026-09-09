package com.indicadoresar.values;

import static org.assertj.core.api.Assertions.assertThat;

import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.support.PostgresContainerSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class IndicatorLatestControllerTest extends PostgresContainerSupport {

    @Autowired
    private IndicatorValueController controller;

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Autowired
    private IndicatorValueRepository indicatorValueRepository;

    @Autowired
    private IndicatorValueService indicatorValueService;

    private Indicator dolar;
    private Indicator ipc;

    @BeforeEach
    void setUp() {
        indicatorValueRepository.deleteAll();
        dolar = indicatorRepository.findByCode("DOLAR_OFICIAL").orElseThrow();
        ipc = indicatorRepository.findByCode("IPC_NACIONAL").orElseThrow();
    }

    @Test
    void getLatestReturnsMostRecent() {
        seedValue(dolar, LocalDate.of(2026, 9, 5), "1200.00");
        seedValue(dolar, LocalDate.of(2026, 9, 6), "1210.00");
        seedValue(dolar, LocalDate.of(2026, 9, 7), "1220.00");

        var response = controller.getLatest("DOLAR_OFICIAL");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().date()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(response.getBody().value()).isEqualByComparingTo(new BigDecimal("1220.00"));
    }

    @Test
    void getLatestIncludesYoyWhenPresent() {
        indicatorValueService.saveOrUpdate(
                ipc, LocalDate.of(2026, 8, 1), new BigDecimal("180.50"), new BigDecimal("45.20"));

        var response = controller.getLatest("IPC_NACIONAL");

        assertThat(response.getBody().yoyValue()).isEqualByComparingTo(new BigDecimal("45.20"));
    }

    @Test
    void getLatestWithYoyNullForBcra() {
        seedValue(dolar, LocalDate.of(2026, 9, 7), "1220.00");

        var response = controller.getLatest("DOLAR_OFICIAL");

        assertThat(response.getBody().yoyValue()).isNull();
    }

    @Test
    void getLatestReturns404WhenCodeUnknown() {
        org.junit.jupiter.api.Assertions.assertThrows(
                com.indicadoresar.common.exception.ResourceNotFoundException.class,
                () -> controller.getLatest("UNKNOWN"));
    }

    @Test
    void getLatestReturns404WhenNoValues() {
        org.junit.jupiter.api.Assertions.assertThrows(
                com.indicadoresar.common.exception.ResourceNotFoundException.class,
                () -> controller.getLatest("RESERVAS_INTERNACIONALES"));
    }

    @Test
    void getLatestDoesNotAffectOtherIndicator() {
        seedValue(dolar, LocalDate.of(2026, 9, 7), "1220.00");

        org.junit.jupiter.api.Assertions.assertThrows(
                com.indicadoresar.common.exception.ResourceNotFoundException.class,
                () -> controller.getLatest("RESERVAS_INTERNACIONALES"));

        var dolarLatest = controller.getLatest("DOLAR_OFICIAL");
        assertThat(dolarLatest.getBody().value()).isEqualByComparingTo(new BigDecimal("1220.00"));
    }

    private void seedValue(Indicator indicator, LocalDate date, String value) {
        indicatorValueService.saveOrUpdate(indicator, date, new BigDecimal(value));
    }
}
