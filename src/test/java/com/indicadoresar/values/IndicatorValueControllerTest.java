package com.indicadoresar.values;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
class IndicatorValueControllerTest extends PostgresContainerSupport {

    @Autowired
    private IndicatorValueService indicatorValueService;

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Autowired
    private IndicatorValueRepository indicatorValueRepository;

    @Autowired
    private IndicatorValueController controller;

    private Indicator dolar;

    @BeforeEach
    void setUp() {
        indicatorValueRepository.deleteAll();
        dolar = indicatorRepository.findByCode("DOLAR_OFICIAL").orElseThrow();
        seedHistory();
    }

    private void seedHistory() {
        seedValue(dolar, LocalDate.of(2026, 9, 5), "1200.00");
        seedValue(dolar, LocalDate.of(2026, 9, 6), "1210.00");
        seedValue(dolar, LocalDate.of(2026, 9, 7), "1220.00");
    }

    private void seedValue(Indicator indicator, LocalDate date, String value) {
        indicatorValueService.saveOrUpdate(indicator, date, new BigDecimal(value));
    }

    @Test
    void findHistoryReturnsOrderedByDateInRange() {
        var result = controller.getHistory("DOLAR_OFICIAL", LocalDate.of(2026, 9, 6), LocalDate.of(2026, 9, 7)).getBody();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 9, 6));
        assertThat(result.get(1).date()).isEqualTo(LocalDate.of(2026, 9, 7));
    }

    @Test
    void findHistoryWithOnlyFromReturnsFromAndAfter() {
        var result =
                controller.getHistory("DOLAR_OFICIAL", LocalDate.of(2026, 9, 6), null).getBody();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 9, 6));
    }

    @Test
    void findHistoryWithOnlyToReturnsUpToTo() {
        var result =
                controller.getHistory("DOLAR_OFICIAL", null, LocalDate.of(2026, 9, 5)).getBody();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 9, 5));
    }

    @Test
    void findHistoryWithNoBoundsReturnsAll() {
        var result = controller.getHistory("DOLAR_OFICIAL", null, null).getBody();

        assertThat(result).hasSize(3);
    }

    @Test
    void findHistoryWithNoDataReturnsEmpty() {
        var result =
                controller.getHistory("DOLAR_OFICIAL", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2))
                        .getBody();

        assertThat(result).isEmpty();
    }

    @Test
    void findHistoryWithInvalidRangeThrowsBadRequest() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> controller.getHistory(
                        "DOLAR_OFICIAL", LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 5)));
    }

    @Test
    void findHistoryWithUnknownCodeThrowsNotFound() {
        org.junit.jupiter.api.Assertions.assertThrows(
                com.indicadoresar.common.exception.ResourceNotFoundException.class,
                () -> controller.getHistory("UNKNOWN", null, null));
    }

    @Test
    void findHistoryIncludesYoyValueWhenPresent() {
        Indicator ipc = indicatorRepository.findByCode("IPC_NACIONAL").orElseThrow();
        indicatorValueService.saveOrUpdate(
                ipc, LocalDate.of(2026, 8, 1), new BigDecimal("180.50"), new BigDecimal("45.20"));

        var result = controller.getHistory("IPC_NACIONAL", null, null).getBody();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).yoyValue()).isEqualByComparingTo(new BigDecimal("45.20"));
    }
}
