package com.indicadoresar.ingestion.indec;

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

@ExtendWith(MockitoExtension.class)
class IndecIpcTaskletTest {

    @Mock
    private IndecClient indecClient;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private IndicatorValueService indicatorValueService;

    private IndecIpcTasklet tasklet;

    private Indicator indicator;

    @BeforeEach
    void setUp() {
        tasklet = new IndecIpcTasklet(indecClient, indicatorRepository, indicatorValueService);
        indicator = createIndicator();
    }

    @Test
    void createsNewValueWhenNoneExists() throws Exception {
        IndecRate rate = new IndecRate(LocalDate.of(2026, 8, 1), new BigDecimal("180.50"), new BigDecimal("45.20"));
        org.mockito.Mockito.when(indicatorRepository.findByCode("IPC_NACIONAL")).thenReturn(Optional.of(indicator));
        org.mockito.Mockito.when(indecClient.fetchIpc()).thenReturn(rate);

        tasklet.execute(null, null);

        org.mockito.Mockito.verify(indicatorValueService)
                .saveOrUpdate(indicator, rate.date(), rate.value(), rate.yoyValue());
    }

    @Test
    void updatesExistingValueWhenAlreadyExists() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 1);
        IndecRate rate = new IndecRate(date, new BigDecimal("182.00"), new BigDecimal("46.00"));

        org.mockito.Mockito.when(indicatorRepository.findByCode("IPC_NACIONAL")).thenReturn(Optional.of(indicator));
        org.mockito.Mockito.when(indecClient.fetchIpc()).thenReturn(rate);

        tasklet.execute(null, null);

        org.mockito.Mockito.verify(indicatorValueService)
                .saveOrUpdate(indicator, date, rate.value(), rate.yoyValue());
    }

    private Indicator createIndicator() {
        Indicator ind =
                new Indicator(
                        "IPC_NACIONAL",
                        "Indice de Precios al Consumidor Nacional",
                        "indice",
                        com.indicadoresar.indicators.IndicatorSource.INDEC,
                        com.indicadoresar.indicators.IndicatorFrequency.MENSUAL,
                        "test");
        try {
            var field = Indicator.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(ind, 4L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ind;
    }
}
