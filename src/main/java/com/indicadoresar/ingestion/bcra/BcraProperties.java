package com.indicadoresar.ingestion.bcra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bcra.api")
public class BcraProperties {

    private String baseUrl = "https://api.bcra.gob.ar";

    private String interestRateVariable = "7";

    private String reservesVariable = "1";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getInterestRateVariable() {
        return interestRateVariable;
    }

    public void setInterestRateVariable(String interestRateVariable) {
        this.interestRateVariable = interestRateVariable;
    }

    public String getReservesVariable() {
        return reservesVariable;
    }

    public void setReservesVariable(String reservesVariable) {
        this.reservesVariable = reservesVariable;
    }
}
