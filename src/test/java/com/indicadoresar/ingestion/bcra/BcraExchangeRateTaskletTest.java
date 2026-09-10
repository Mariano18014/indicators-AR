package com.indicadoresar.ingestion.bcra;

import static org.mockito.Mockito.when;

import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.values.IndicatorValueService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

@ExtendWith(MockitoExtension.class)
class BcraExchangeRateTaskletTest {

    @Mock
    private BcraClient bcraClient;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private IndicatorValueService indicatorValueService;

    @Mock
    private RetryTemplate retryTemplate;

    private BcraExchangeRateTasklet tasklet;

    private Indicator indicator;

    @BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.lenient()
                .when(retryTemplate.execute(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(
                        invocation -> {
                            RetryCallback<RepeatStatus, Exception> callback = invocation.getArgument(0);
                            return callback.doWithRetry(
                                    org.mockito.Mockito.mock(RetryContext.class));
                        });
        tasklet = new BcraExchangeRateTasklet(bcraClient, indicatorRepository, indicatorValueService, retryTemplate);
        indicator = createIndicator();
    }

    @Test
    void createsNewValueWhenNoneExists() throws Exception {
        BcraRate rate = new BcraRate(LocalDate.of(2026, 9, 7), new BigDecimal("1210.75"));
        when(indicatorRepository.findByCode("DOLAR_OFICIAL")).thenReturn(Optional.of(indicator));
        when(bcraClient.fetchExchangeRate()).thenReturn(rate);

        tasklet.execute(null, null);

        org.mockito.Mockito.verify(indicatorValueService).saveOrUpdate(indicator, rate.date(), rate.value());
    }

    @Test
    void updatesExistingValueWhenAlreadyExists() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 7);
        BcraRate rate = new BcraRate(date, new BigDecimal("1220.00"));

        when(indicatorRepository.findByCode("DOLAR_OFICIAL")).thenReturn(Optional.of(indicator));
        when(bcraClient.fetchExchangeRate()).thenReturn(rate);

        tasklet.execute(null, null);

        org.mockito.Mockito.verify(indicatorValueService).saveOrUpdate(indicator, date, rate.value());
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
