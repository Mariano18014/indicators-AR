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
class IndicatorValueServiceTest extends PostgresContainerSupport {

    @Autowired
    private IndicatorValueService indicatorValueService;

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Autowired
    private IndicatorValueRepository indicatorValueRepository;

    private Indicator dolarIndicator;

    @BeforeEach
    void cleanDatabase() {
        indicatorValueRepository.deleteAll();
        dolarIndicator = findDolarIndicator();
    }

    private Indicator findDolarIndicator() {
        return indicatorRepository.findByCode("DOLAR_OFICIAL").orElseThrow();
    }

    @Test
    void saveOrUpdateCreatesNewWhenNotExists() {
        LocalDate date = LocalDate.of(2026, 9, 7);
        BigDecimal value = new BigDecimal("1210.7500");

        IndicatorValue saved = indicatorValueService.saveOrUpdate(dolarIndicator, date, value);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getValue()).isEqualByComparingTo(value);
        assertThat(indicatorValueRepository.count()).isEqualTo(1);
    }

    @Test
    void saveOrUpdateUpdatesExistingWhenSameDate() {
        LocalDate date = LocalDate.of(2026, 9, 7);
        BigDecimal firstValue = new BigDecimal("1210.7500");
        BigDecimal secondValue = new BigDecimal("1220.0000");

        indicatorValueService.saveOrUpdate(dolarIndicator, date, firstValue);
        IndicatorValue updated = indicatorValueService.saveOrUpdate(dolarIndicator, date, secondValue);

        assertThat(indicatorValueRepository.count()).isEqualTo(1);
        assertThat(updated.getValue()).isEqualByComparingTo(secondValue);
        assertThat(updated.getId()).isNotNull();
    }

    @Test
    void saveOrUpdateIsIdempotentAcrossMultipleCalls() {
        LocalDate date = LocalDate.of(2026, 9, 7);
        indicatorValueService.saveOrUpdate(dolarIndicator, date, new BigDecimal("100.0000"));
        indicatorValueService.saveOrUpdate(dolarIndicator, date, new BigDecimal("100.0000"));
        indicatorValueService.saveOrUpdate(dolarIndicator, date, new BigDecimal("100.0000"));

        assertThat(indicatorValueRepository.count()).isEqualTo(1);
    }

    @Test
    void saveOrUpdateCreatesSeparateRowsForDifferentDates() {
        indicatorValueService.saveOrUpdate(dolarIndicator, LocalDate.of(2026, 9, 6), new BigDecimal("1210.0000"));
        indicatorValueService.saveOrUpdate(dolarIndicator, LocalDate.of(2026, 9, 7), new BigDecimal("1220.0000"));

        assertThat(indicatorValueRepository.count()).isEqualTo(2);
    }


}
