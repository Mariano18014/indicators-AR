package com.indicadoresar.ingestion.bcra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class BcraClientTest {

    private BcraClient bcraClient;

    @BeforeEach
    void setUp() {
        BcraProperties properties = new BcraProperties();
        properties.setBaseUrl("https://api.bcra.gob.ar");
        ObjectMapper objectMapper = new ObjectMapper();
        RestClient.Builder builder = RestClient.builder();
        bcraClient = new BcraClient(builder, properties, objectMapper);
    }

    @Test
    void parseExchangeRateResponseExtractsUsdEntry() {
        String json =
                """
                {
                  "status": 200,
                  "results": {
                    "fecha": "2026-09-07",
                    "detalle": [
                      {"codigoMoneda": "EUR", "tipoCotizacion": "1300.00"},
                      {"codigoMoneda": "USD", "tipoCotizacion": "1210.75"}
                    ]
                  }
                }
                """;

        BcraRate result = bcraClient.parseExchangeRateResponse(json);

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("1210.75"));
    }

    @Test
    void parseExchangeRateResponseThrowsWhenResultsMissing() {
        String json = """
                {"other": {}}
                """;

        assertThatThrownBy(() -> bcraClient.parseExchangeRateResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("results");
    }

    @Test
    void parseExchangeRateResponseThrowsWhenFechaIsNull() {
        String json = """
                {"results": {"fecha": null, "detalle": []}}
                """;

        assertThatThrownBy(() -> bcraClient.parseExchangeRateResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fecha");
    }

    @Test
    void parseExchangeRateResponseThrowsWhenDetalleEmpty() {
        String json = """
                {"results": {"fecha": "2026-09-07", "detalle": []}}
                """;

        assertThatThrownBy(() -> bcraClient.parseExchangeRateResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("detalle");
    }

    @Test
    void parseExchangeRateResponseThrowsWhenUsdEntryMissing() {
        String json =
                """
                {"results": {"fecha": "2026-09-07", "detalle": [{"codigoMoneda": "EUR", "tipoCotizacion": "1300.00"}]}}
                """;

        assertThatThrownBy(() -> bcraClient.parseExchangeRateResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("USD");
    }

    @Test
    void parseExchangeRateResponseThrowsWhenJsonInvalid() {
        String json = "not json";

        assertThatThrownBy(() -> bcraClient.parseExchangeRateResponse(json))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void parseExchangeRateResponseThrowsWhenDateInvalid() {
        String json =
                """
                {"results": {"fecha": "not-a-date", "detalle": [{"codigoMoneda": "USD", "tipoCotizacion": "1210.75"}]}}
                """;

        assertThatThrownBy(() -> bcraClient.parseExchangeRateResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid date");
    }

    @Test
    void parseMonetariaResponseExtractsMostRecentEntry() {
        String json =
                """
                {
                  "status": 200,
                  "results": [
                    {
                      "idVariable": 7,
                      "detalle": [
                        {"fecha": "2026-09-07", "valor": "22.12"},
                        {"fecha": "2026-09-04", "valor": "22.06"}
                      ]
                    }
                  ]
                }
                """;

        BcraRate result = bcraClient.parseMonetariaResponse(json);

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("22.12"));
    }

    @Test
    void parseMonetariaResponseThrowsWhenResultsEmpty() {
        String json = """
                {"results": []}
                """;

        assertThatThrownBy(() -> bcraClient.parseMonetariaResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("results");
    }

    @Test
    void parseMonetariaResponseThrowsWhenDetalleEmpty() {
        String json = """
                {"results": [{"idVariable": 7, "detalle": []}]}
                """;

        assertThatThrownBy(() -> bcraClient.parseMonetariaResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("detalle");
    }

    @Test
    void parseMonetariaResponseThrowsWhenJsonInvalid() {
        String json = "not json";

        assertThatThrownBy(() -> bcraClient.parseMonetariaResponse(json))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void parseMonetariaResponseThrowsWhenDateInvalid() {
        String json =
                """
                {"results": [{"idVariable": 7, "detalle": [{"fecha": "not-a-date", "valor": "22.12"}]}]}
                """;

        assertThatThrownBy(() -> bcraClient.parseMonetariaResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid date");
    }
}
