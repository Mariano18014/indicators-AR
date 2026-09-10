package com.indicadoresar.ingestion.bcra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BcraClient {

    private static final Logger log = LoggerFactory.getLogger(BcraClient.class);
    private static final DateTimeFormatter BCRA_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CAMBIARIAS_COTIZACIONES_PATH = "/estadisticascambiarias/v1.0/Cotizaciones";
    private static final String USD_CURRENCY_CODE = "USD";

    private final RestClient restClient;
    private final BcraProperties properties;
    private final ObjectMapper objectMapper;

    public BcraClient(RestClient.Builder builder, BcraProperties properties, ObjectMapper objectMapper) {
        this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public BcraRate fetchExchangeRate() {
        String rawResponse = callBcraApi(CAMBIARIAS_COTIZACIONES_PATH, "exchange rate");
        return parseExchangeRateResponse(rawResponse);
    }

    public BcraRate fetchInterestRate() {
        return fetchMonetariaVariable(properties.getInterestRateVariable(), "interest rate");
    }

    public BcraRate fetchReserves() {
        return fetchMonetariaVariable(properties.getReservesVariable(), "reserves");
    }

    private BcraRate fetchMonetariaVariable(String variable, String label) {
        String rawResponse = callBcraApi(buildMonetariasPath(variable), label);
        return parseMonetariaResponse(rawResponse);
    }

    private String buildMonetariasPath(String variable) {
        return "/estadisticas/v4.0/monetarias/" + variable;
    }

    private String callBcraApi(String path, String label) {
        log.info("Fetching BCRA {} from {}", label, path);
        String response = restClient.get().uri(path).retrieve().body(String.class);
        validateResponse(response);
        return response;
    }

    private void validateResponse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("Empty response from BCRA API");
        }
    }

    BcraRate parseExchangeRateResponse(String rawResponse) {
        JsonNode results = extractCambiariasResults(parseJson(rawResponse));
        LocalDate date = extractCambiariasDate(results);
        JsonNode usdEntry = findUsdEntry(extractCambiariasDetalle(results));
        BigDecimal value = extractCambiariasValue(usdEntry);
        return new BcraRate(date, value);
    }

    private JsonNode extractCambiariasResults(JsonNode root) {
        JsonNode results = root.get("results");
        if (results == null || !results.isObject()) {
            throw new IllegalStateException("BCRA cambiarias response missing 'results' object");
        }
        return results;
    }

    private LocalDate extractCambiariasDate(JsonNode results) {
        JsonNode dateNode = results.get("fecha");
        if (dateNode == null || dateNode.isNull()) {
            throw new IllegalStateException("BCRA cambiarias response missing 'fecha'");
        }
        return parseDate(dateNode.asText());
    }

    private JsonNode extractCambiariasDetalle(JsonNode results) {
        JsonNode detalle = results.get("detalle");
        if (detalle == null || !detalle.isArray() || detalle.isEmpty()) {
            throw new IllegalStateException("BCRA cambiarias response missing 'detalle' array");
        }
        return detalle;
    }

    private JsonNode findUsdEntry(JsonNode detalle) {
        for (JsonNode entry : detalle) {
            JsonNode code = entry.get("codigoMoneda");
            if (code != null && USD_CURRENCY_CODE.equals(code.asText())) {
                return entry;
            }
        }
        throw new IllegalStateException("BCRA cambiarias response missing USD entry");
    }

    private BigDecimal extractCambiariasValue(JsonNode entry) {
        JsonNode valueNode = entry.get("tipoCotizacion");
        if (valueNode == null || valueNode.isNull()) {
            throw new IllegalStateException("BCRA cambiarias entry missing 'tipoCotizacion': " + entry);
        }
        return parseValue(valueNode.asText());
    }

    BcraRate parseMonetariaResponse(String rawResponse) {
        JsonNode latestEntry = extractLatestMonetariaEntry(parseJson(rawResponse));
        LocalDate date = parseDate(extractMonetariaField(latestEntry, "fecha").asText());
        BigDecimal value = parseValue(extractMonetariaField(latestEntry, "valor").asText());
        return new BcraRate(date, value);
    }

    private JsonNode extractLatestMonetariaEntry(JsonNode root) {
        JsonNode results = root.get("results");
        if (results == null || !results.isArray() || results.isEmpty()) {
            throw new IllegalStateException("BCRA monetarias response missing 'results' array");
        }
        JsonNode detalle = results.get(0).get("detalle");
        if (detalle == null || !detalle.isArray() || detalle.isEmpty()) {
            throw new IllegalStateException("BCRA monetarias response missing 'detalle' array");
        }
        // BCRA v4 monetarias returns 'detalle' ordered from most recent to oldest
        return detalle.get(0);
    }

    private JsonNode extractMonetariaField(JsonNode entry, String field) {
        JsonNode node = entry.get(field);
        if (node == null || node.isNull()) {
            throw new IllegalStateException("BCRA monetarias entry missing '" + field + "': " + entry);
        }
        return node;
    }

    private JsonNode parseJson(String rawResponse) {
        try {
            return objectMapper.readTree(rawResponse);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse BCRA JSON response", e);
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, BCRA_DATE);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid date in BCRA response: " + dateStr, e);
        }
    }

    private BigDecimal parseValue(String valueStr) {
        try {
            return new BigDecimal(valueStr);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid numeric value in BCRA response: " + valueStr, e);
        }
    }
}
