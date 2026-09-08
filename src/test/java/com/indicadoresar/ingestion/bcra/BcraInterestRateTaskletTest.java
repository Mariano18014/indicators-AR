package com.indicadoresar.ingestion.bcra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.values.IndicatorValue;
import com.indicadoresar.values.IndicatorValueRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BcraInterestRateTaskletTest {

    @Mock
    private BcraClient bcraClient;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private IndicatorValueRepository indicatorValueRepository;

    private BcraInterestRateTasklet tasklet;

    private Indicator indicator;

    @BeforeEach
    void setUp() {
        tasklet = new BcraInterestRateTasklet(bcraClient, indicatorRepository, indicatorValueRepository);
        indicator = createIndicator();
    }

    @Test
    void createsNewValueWhenNoneExists() throws Exception {
        BcraRate rate = new BcraRate(LocalDate.of(2026, 9, 7), new BigDecimal("45.50"));
        when(indicatorRepository.findByCode("TASA_POLITICA_MONETARIA")).thenReturn(Optional.of(indicator));
        when(bcraClient.fetchInterestRate()).thenReturn(rate);
        when(indicatorValueRepository.findByIndicatorIdAndDate(2L, rate.date()))
                .thenReturn(Optional.empty());

        tasklet.execute(null, null);

        ArgumentCaptor<IndicatorValue> captor = ArgumentCaptor.forClass(IndicatorValue.class);
        org.mockito.Mockito.verify(indicatorValueRepository).save(captor.capture());
        IndicatorValue saved = captor.getValue();
        assertThat(saved.getIndicator()).isEqualTo(indicator);
        assertThat(saved.getDate()).isEqualTo(rate.date());
        assertThat(saved.getValue()).isEqualByComparingTo(rate.value());
    }

    @Test
    void updatesExistingValueWhenAlreadyExists() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 7);
        BcraRate rate = new BcraRate(date, new BigDecimal("47.00"));
        IndicatorValue existing = new IndicatorValue(indicator, date, new BigDecimal("45.50"), Instant.now());

        when(indicatorRepository.findByCode("TASA_POLITICA_MONETARIA")).thenReturn(Optional.of(indicator));
        when(bcraClient.fetchInterestRate()).thenReturn(rate);
        when(indicatorValueRepository.findByIndicatorIdAndDate(2L, date))
                .thenReturn(Optional.of(existing));

        tasklet.execute(null, null);

        org.mockito.Mockito.verify(indicatorValueRepository).save(existing);
        assertThat(existing.getValue()).isEqualByComparingTo(new BigDecimal("47.00"));
    }

    private Indicator createIndicator() {
        Indicator ind =
                new Indicator(
                        "TASA_POLITICA_MONETARIA",
                        "Tasa de Politica Monetaria",
                        "%",
                        com.indicadoresar.indicators.IndicatorSource.BCRA,
                        com.indicadoresar.indicators.IndicatorFrequency.DIARIA,
                        "test");
        try {
            var field = Indicator.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(ind, 2L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ind;
    }
}
