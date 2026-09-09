package com.indicadoresar.indicators;

import static org.assertj.core.api.Assertions.assertThat;

import com.indicadoresar.support.PostgresContainerSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class IndicatorControllerTest extends PostgresContainerSupport {

    @Autowired
    private IndicatorController indicatorController;

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Test
    void getIndicatorsReturnsAllSeedsOrderedByCode() {
        var response = indicatorController.getIndicators();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<IndicatorResponse> body = response.getBody();
        assertThat(body).hasSize(4);
        assertThat(body.get(0).code()).isEqualTo("DOLAR_OFICIAL");
        assertThat(body.get(1).code()).isEqualTo("IPC_NACIONAL");
        assertThat(body.get(2).code()).isEqualTo("RESERVAS_INTERNACIONALES");
        assertThat(body.get(3).code()).isEqualTo("TASA_POLITICA_MONETARIA");
    }

    @Test
    void getIndicatorsReturnsEmptyWhenNone() {
        indicatorRepository.deleteAll();

        var response = indicatorController.getIndicators();

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getIndicatorsIncludesAllFields() {
        var response = indicatorController.getIndicators();
        IndicatorResponse dolar = findByCode(response.getBody(), "DOLAR_OFICIAL");

        assertThat(dolar.name()).isNotBlank();
        assertThat(dolar.unit()).isNotBlank();
        assertThat(dolar.source()).isEqualTo("BCRA");
        assertThat(dolar.frequency()).isEqualTo("DIARIA");
        assertThat(dolar.description()).isNotBlank();
    }

    @Test
    void getIndicatorsIncludesIndecFields() {
        var response = indicatorController.getIndicators();
        IndicatorResponse ipc = findByCode(response.getBody(), "IPC_NACIONAL");

        assertThat(ipc.source()).isEqualTo("INDEC");
        assertThat(ipc.frequency()).isEqualTo("MENSUAL");
    }

    private IndicatorResponse findByCode(List<IndicatorResponse> indicators, String code) {
        return indicators.stream()
                .filter(i -> i.code().equals(code))
                .findFirst()
                .orElseThrow();
    }
}
