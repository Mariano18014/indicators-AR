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

    private final RestClient restClient;
    private final BcraProperties properties;
    private final ObjectMapper objectMapper;

    public BcraClient(RestClient.Builder builder, BcraProperties properties, ObjectMapper objectMapper) {
        this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public BcraRate fetchExchangeRate() {
        return fetchByVariable(properties.getExchangeRateVariable(), "exchange rate");
    }

    public BcraRate fetchInterestRate() {
        return fetchByVariable(properties.getInterestRateVariable(), "interest rate");
    }

    public BcraRate fetchReserves() {
        return fetchByVariable(properties.getReservesVariable(), "reserves");
    }

    private BcraRate fetchByVariable(String variable, String label) {
        String rawResponse = callBcraApi(variable, label);
        return parseResponse(rawResponse);
    }

    private String callBcraApi(String variable, String label) {
        String path = buildPath(variable);
        log.info("Fetching BCRA {} from {}", label, path);
        String response = restClient.get().uri(path).retrieve().body(String.class);
        validateResponse(response);
        return response;
    }

    private String buildPath(String variable) {
        return "/estadisticas/v3.0/monetarias/" + variable;
    }

    private void validateResponse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("Empty response from BCRA API");
        }
    }

    BcraRate parseExchangeRateResponse(String rawResponse) {
        return parseResponse(rawResponse);
    }

    BcraRate parseInterestRateResponse(String rawResponse) {
        return parseResponse(rawResponse);
    }

    BcraRate parseReservesResponse(String rawResponse) {
        return parseResponse(rawResponse);
    }

    BcraRate parseResponse(String rawResponse) {
        JsonNode root = parseJson(rawResponse);
        JsonNode results = extractResultsArray(root);
        JsonNode latestEntry = findLatestEntry(results);
        return buildRate(latestEntry);
    }

    private JsonNode parseJson(String rawResponse) {
        try {
            return objectMapper.readTree(rawResponse);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse BCRA JSON response", e);
        }
    }

    private JsonNode extractResultsArray(JsonNode root) {
        JsonNode results = root.get("results");
        if (results == null || !results.isArray() || results.isEmpty()) {
            throw new IllegalStateException("BCRA response missing or empty 'results' array");
        }
        return results;
    }

    private JsonNode findLatestEntry(JsonNode results) {
        // BCRA returns chronological order; last element is most recent
        return results.get(results.size() - 1);
    }

    private BcraRate buildRate(JsonNode entry) {
        LocalDate date = extractDate(entry);
        BigDecimal value = extractValue(entry);
        return new BcraRate(date, value);
    }

    private LocalDate extractDate(JsonNode entry) {
        JsonNode dateNode = entry.get("fecha");
        if (dateNode == null || dateNode.isNull()) {
            dateNode = entry.get("date");
        }
        if (dateNode == null || dateNode.isNull()) {
            throw new IllegalStateException("BCRA entry missing 'fecha' field: " + entry);
        }
        String dateStr = dateNode.asText();
        try {
            return LocalDate.parse(dateStr, BCRA_DATE);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid date format in BCRA response: " + dateStr, e);
        }
    }

    private BigDecimal extractValue(JsonNode entry) {
        JsonNode valueNode = entry.get("valor");
        if (valueNode == null || valueNode.isNull()) {
            valueNode = entry.get("value");
        }
        if (valueNode == null || valueNode.isNull()) {
            throw new IllegalStateException("BCRA entry missing 'valor' field: " + entry);
        }
        try {
            return new BigDecimal(valueNode.asText());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid numeric value in BCRA response: " + valueNode, e);
        }
    }
}
