package com.indicadoresar.values;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorFrequency;
import com.indicadoresar.indicators.IndicatorSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class IndicatorValueServiceUnitTest {

    @Test
    void saveOrUpdateHandlesRaceConditionViaRetry() {
        Indicator indicator = createIndicator();
        LocalDate date = LocalDate.of(2026, 9, 7);
        BigDecimal value = new BigDecimal("1300.0000");

        IndicatorValueRepository mockRepository = mock(IndicatorValueRepository.class);
        IndicatorValueService serviceWithMock =
                new IndicatorValueService(mockRepository, mock(com.indicadoresar.indicators.IndicatorRepository.class));

        when(mockRepository.findByIndicatorIdAndDate(indicator.getId(), date))
                .thenReturn(java.util.Optional.empty())
                .thenReturn(java.util.Optional.of(
                        new IndicatorValue(indicator, date, new BigDecimal("1200.0000"), java.time.Instant.now())));

        when(mockRepository.save(any(IndicatorValue.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IndicatorValue result = serviceWithMock.saveOrUpdate(indicator, date, value);

        assertThat(result).isNotNull();
    }

    private Indicator createIndicator() {
        Indicator ind =
                new Indicator(
                        "DOLAR_OFICIAL",
                        "Dolar Oficial",
                        "ARS",
                        IndicatorSource.BCRA,
                        IndicatorFrequency.DIARIA,
                        "test");
        try {
            var field = Indicator.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(ind, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ind;
    }
}
