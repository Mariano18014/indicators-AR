package com.indicadoresar.ingestion.bcra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bcra.api")
public class BcraProperties {

    private String baseUrl = "https://api.bcra.gob.ar";

    private String exchangeRateVariable = "1";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getExchangeRateVariable() {
        return exchangeRateVariable;
    }

    public void setExchangeRateVariable(String exchangeRateVariable) {
        this.exchangeRateVariable = exchangeRateVariable;
    }
}
