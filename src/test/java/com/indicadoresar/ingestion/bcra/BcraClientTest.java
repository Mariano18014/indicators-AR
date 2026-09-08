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
        properties.setExchangeRateVariable("1");
        ObjectMapper objectMapper = new ObjectMapper();
        RestClient.Builder builder = RestClient.builder();
        bcraClient = new BcraClient(builder, properties, objectMapper);
    }

    @Test
    void parseExchangeRateResponseExtractsLatestEntry() {
        String json =
                """
                {
                  "results": [
                    {"fecha": "2026-09-06", "valor": "1200.50"},
                    {"fecha": "2026-09-07", "valor": "1210.75"}
                  ]
                }
                """;

        BcraExchangeRate result = bcraClient.parseExchangeRateResponse(json);

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("1210.75"));
    }

    @Test
    void parseExchangeRateResponseSupportsEnglishFields() {
        String json =
                """
                {
                  "results": [
                    {"date": "2026-09-07", "value": "1210.75"}
                  ]
                }
                """;

        BcraExchangeRate result = bcraClient.parseExchangeRateResponse(json);

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("1210.75"));
    }

    @Test
    void parseExchangeRateResponseThrowsWhenResultsEmpty() {
        String json = """
                {"results": []}
                """;

        assertThatThrownBy(() -> bcraClient.parseExchangeRateResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("results");
    }

    @Test
    void parseExchangeRateResponseThrowsWhenResultsMissing() {
        String json = """
                {"other": []}
                """;

        assertThatThrownBy(() -> bcraClient.parseExchangeRateResponse(json))
                .isInstanceOf(IllegalStateException.class);
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
                {"results": [{"fecha": "not-a-date", "valor": "100"}]}
                """;

        assertThatThrownBy(() -> bcraClient.parseExchangeRateResponse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid date");
    }
}
