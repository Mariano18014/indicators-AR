package com.indicadoresar.ingestion.bcra;

import static org.assertj.core.api.Assertions.assertThat;
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

@ExtendWith(MockitoExtension.class)
class BcraReservesTaskletTest {

    @Mock
    private BcraClient bcraClient;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private IndicatorValueService indicatorValueService;

    private BcraReservesTasklet tasklet;

    private Indicator indicator;

    @BeforeEach
    void setUp() {
        tasklet = new BcraReservesTasklet(bcraClient, indicatorRepository, indicatorValueService);
        indicator = createIndicator();
    }

    @Test
    void createsNewValueWhenNoneExists() throws Exception {
        BcraRate rate = new BcraRate(LocalDate.of(2026, 9, 7), new BigDecimal("28500.50"));
        when(indicatorRepository.findByCode("RESERVAS_INTERNACIONALES")).thenReturn(Optional.of(indicator));
        when(bcraClient.fetchReserves()).thenReturn(rate);

        tasklet.execute(null, null);

        org.mockito.Mockito.verify(indicatorValueService).saveOrUpdate(indicator, rate.date(), rate.value());
    }

    @Test
    void updatesExistingValueWhenAlreadyExists() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 7);
        BcraRate rate = new BcraRate(date, new BigDecimal("28700.00"));

        when(indicatorRepository.findByCode("RESERVAS_INTERNACIONALES")).thenReturn(Optional.of(indicator));
        when(bcraClient.fetchReserves()).thenReturn(rate);

        tasklet.execute(null, null);

        org.mockito.Mockito.verify(indicatorValueService).saveOrUpdate(indicator, date, rate.value());
    }

    private Indicator createIndicator() {
        Indicator ind =
                new Indicator(
                        "RESERVAS_INTERNACIONALES",
                        "Reservas Internacionales",
                        "USD millones",
                        com.indicadoresar.indicators.IndicatorSource.BCRA,
                        com.indicadoresar.indicators.IndicatorFrequency.DIARIA,
                        "test");
        try {
            var field = Indicator.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(ind, 3L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ind;
    }
}
