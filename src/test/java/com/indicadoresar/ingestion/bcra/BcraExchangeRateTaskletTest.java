package com.indicadoresar.ingestion.bcra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.values.IndicatorValue;
import com.indicadoresar.values.IndicatorValueRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BcraExchangeRateTaskletTest {

    @Mock
    private BcraClient bcraClient;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private IndicatorValueRepository indicatorValueRepository;

    private BcraExchangeRateTasklet tasklet;

    private Indicator indicator;

    @BeforeEach
    void setUp() {
        tasklet = new BcraExchangeRateTasklet(bcraClient, indicatorRepository, indicatorValueRepository);
        indicator = createIndicator();
    }

    @Test
    void createsNewValueWhenNoneExists() throws Exception {
        BcraExchangeRate rate = new BcraExchangeRate(LocalDate.of(2026, 9, 7), new BigDecimal("1210.75"));
        when(indicatorRepository.findByCode("DOLAR_OFICIAL")).thenReturn(Optional.of(indicator));
        when(bcraClient.fetchExchangeRate()).thenReturn(rate);
        when(indicatorValueRepository.findByIndicatorIdAndDate(1L, rate.date()))
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
        BcraExchangeRate rate = new BcraExchangeRate(date, new BigDecimal("1220.00"));
        IndicatorValue existing = new IndicatorValue(indicator, date, new BigDecimal("1210.75"), java.time.Instant.now());

        when(indicatorRepository.findByCode("DOLAR_OFICIAL")).thenReturn(Optional.of(indicator));
        when(bcraClient.fetchExchangeRate()).thenReturn(rate);
        when(indicatorValueRepository.findByIndicatorIdAndDate(1L, date))
                .thenReturn(Optional.of(existing));

        tasklet.execute(null, null);

        org.mockito.Mockito.verify(indicatorValueRepository).save(existing);
        assertThat(existing.getValue()).isEqualByComparingTo(new BigDecimal("1220.00"));
    }

    private Indicator createIndicator() {
        // Use reflection to set id since JPA generates it
        Indicator ind =
                new Indicator(
                        "DOLAR_OFICIAL",
                        "Dolar Oficial",
                        "ARS",
                        com.indicadoresar.indicators.IndicatorSource.BCRA,
                        com.indicadoresar.indicators.IndicatorFrequency.DIARIA,
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
