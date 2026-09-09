package com.indicadoresar.ingestion.indec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class IndecClientTest {

    private IndecClient indecClient;

    @BeforeEach
    void setUp() {
        IndecProperties properties = new IndecProperties();
        properties.setBaseUrl("https://apis.datos.gob.ar/series/api/series/");
        properties.setIpcSeriesId("148.3_INIVELNAL_DICI_M_26");
        properties.setFormat("json");
        ObjectMapper objectMapper = new ObjectMapper();
        RestClient.Builder builder = RestClient.builder();
        indecClient = new IndecClient(builder, properties, objectMapper);
    }

    @Test
    void parseResponseExtractsLatestEntryWithFechaIndice() {
        String json =
                """
                {
                  "data": [
                    ["2026-07-01", "178.20"],
                    ["2026-08-01", "180.50"]
                  ]
                }
                """;

        IndecRate result = indecClient.parseResponse(json);

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("180.50"));
    }

    @Test
    void parseResponseSupportsObjectFormatWithFechaIndice() {
        String json =
                """
                {
                  "data": [
                    {"fecha": "2026-07-01", "indice": "178.20"},
                    {"fecha": "2026-08-01", "indice": "180.50"}
                  ]
                }
                """;

        IndecRate result = indecClient.parseResponse(json);

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("180.50"));
    }

    @Test
    void parseResponseSupportsYearMonthFormat() {
        String json =
                """
                {
                  "data": [
                    ["2026-08", "180.50"]
                  ]
                }
                """;

        IndecRate result = indecClient.parseResponse(json);

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("180.50"));
    }

    @Test
    void parseResponseSupportsResultsArrayFallback() {
        String json =
                """
                {
                  "results": [
                    {"fecha": "2026-08-01", "valor": "180.50"}
                  ]
                }
                """;

        IndecRate result = indecClient.parseResponse(json);

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("180.50"));
    }

    @Test
    void parseResponseThrowsWhenDataEmpty() {
        String json = """
                {"data": []}
                """;

        assertThatThrownBy(() -> indecClient.parseResponse(json))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void parseResponseThrowsWhenDataMissing() {
        String json = """
                {"other": []}
                """;

        assertThatThrownBy(() -> indecClient.parseResponse(json))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void parseResponseThrowsWhenJsonInvalid() {
        String json = "not json";

        assertThatThrownBy(() -> indecClient.parseResponse(json))
                .isInstanceOf(IllegalStateException.class);
    }
}
