package com.indicadoresar.ingestion.indec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IndecClient {

    private static final Logger log = LoggerFactory.getLogger(IndecClient.class);
    private static final DateTimeFormatter INDEC_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter INDEC_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    private final RestClient restClient;
    private final IndecProperties properties;
    private final ObjectMapper objectMapper;

    public IndecClient(RestClient.Builder builder, IndecProperties properties, ObjectMapper objectMapper) {
        this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public IndecRate fetchIpc() {
        return fetchIpcWithYoy();
    }

    private IndecRate fetchIpcWithYoy() {
        String valuePath = buildPath(properties.getIpcSeriesId());
        String yoyPath = buildPath(properties.getIpcSeriesId() + properties.getIpcYoySuffix());
        String valueResponse = callIndecApi(valuePath, "IPC");
        String yoyResponse = callIndecApiSafe(yoyPath, "IPC YoY");
        IndecRate valueRate = parseResponse(valueResponse);
        BigDecimal yoyValue = parseYoyResponse(yoyResponse);
        return new IndecRate(valueRate.date(), valueRate.value(), yoyValue);
    }

    private String callIndecApi() {
        String path = buildPath(properties.getIpcSeriesId());
        return callIndecApi(path, "IPC");
    }

    private String callIndecApi(String path, String label) {
        log.info("Fetching INDEC {} from {}", label, path);
        String response = restClient.get().uri(path).retrieve().body(String.class);
        validateResponse(response);
        return response;
    }

    private String callIndecApiSafe(String path, String label) {
        try {
            return callIndecApi(path, label);
        } catch (Exception e) {
            log.warn("Failed to fetch INDEC {} (yoy may be null): {}", label, e.getMessage());
            return null;
        }
    }

    private String buildPath() {
        return buildPath(properties.getIpcSeriesId());
    }

    private String buildPath(String seriesId) {
        return "?ids=" + seriesId + "&format=" + properties.getFormat() + "&limit=1&sort=desc";
    }

    private void validateResponse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("Empty response from INDEC API");
        }
    }

    IndecRate parseResponse(String rawResponse) {
        JsonNode root = parseJson(rawResponse);
        JsonNode dataArray = extractDataArray(root);
        JsonNode latestEntry = findLatestEntry(dataArray);
        return buildRate(latestEntry);
    }

    private BigDecimal parseYoyResponse(String yoyResponse) {
        if (yoyResponse == null || yoyResponse.isBlank()) {
            return null;
        }
        try {
            JsonNode root = parseJson(yoyResponse);
            JsonNode dataArray = extractDataArray(root);
            JsonNode latestEntry = findLatestEntry(dataArray);
            return extractValue(latestEntry);
        } catch (Exception e) {
            log.warn("Failed to parse INDEC YoY response, yoy will be null: {}", e.getMessage());
            return null;
        }
    }

    private JsonNode parseJson(String rawResponse) {
        try {
            return objectMapper.readTree(rawResponse);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse INDEC JSON response", e);
        }
    }

    private JsonNode extractDataArray(JsonNode root) {
        JsonNode data = root.get("data");
        if (data != null && data.isArray() && !data.isEmpty()) {
            return data;
        }
        JsonNode results = root.get("results");
        if (results != null && results.isArray() && !results.isEmpty()) {
            return results;
        }
        throw new IllegalStateException("INDEC response missing or empty 'data' array");
    }

    private JsonNode findLatestEntry(JsonNode data) {
        return data.get(data.size() - 1);
    }

    private IndecRate buildRate(JsonNode entry) {
        LocalDate date = extractDate(entry);
        BigDecimal value = extractValue(entry);
        return new IndecRate(date, value);
    }

    private LocalDate extractDate(JsonNode entry) {
        JsonNode dateNode = findDateNode(entry);
        if (dateNode == null || dateNode.isNull()) {
            throw new IllegalStateException("INDEC entry missing date field: " + entry);
        }
        return parseDate(dateNode.asText());
    }

    private JsonNode findDateNode(JsonNode entry) {
        if (entry.isArray() && entry.size() >= 1) {
            return entry.get(0);
        }
        JsonNode node = entry.get("fecha");
        if (node != null && !node.isNull()) return node;
        node = entry.get("date");
        if (node != null && !node.isNull()) return node;
        node = entry.get("period");
        if (node != null && !node.isNull()) return node;
        return null;
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, INDEC_DATE);
        } catch (Exception e) {
            try {
                YearMonth ym = YearMonth.parse(dateStr, INDEC_MONTH);
                return ym.atDay(1);
            } catch (Exception ex) {
                throw new IllegalStateException("Invalid date format in INDEC response: " + dateStr, ex);
            }
        }
    }

    private BigDecimal extractValue(JsonNode entry) {
        JsonNode valueNode = findValueNode(entry);
        if (valueNode == null || valueNode.isNull()) {
            throw new IllegalStateException("INDEC entry missing value field: " + entry);
        }
        try {
            return new BigDecimal(valueNode.asText());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid numeric value in INDEC response: " + valueNode, e);
        }
    }

    private JsonNode findValueNode(JsonNode entry) {
        if (entry.isArray() && entry.size() >= 2) {
            return entry.get(1);
        }
        JsonNode node = entry.get("indice");
        if (node != null && !node.isNull()) return node;
        node = entry.get("valor");
        if (node != null && !node.isNull()) return node;
        node = entry.get("value");
        if (node != null && !node.isNull()) return node;
        return null;
    }
}
