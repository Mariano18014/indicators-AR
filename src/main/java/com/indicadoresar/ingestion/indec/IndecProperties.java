package com.indicadoresar.ingestion.indec;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indec.api")
public class IndecProperties {

    private String baseUrl = "https://apis.datos.gob.ar/series/api/series/";

    private String ipcSeriesId = "148.3_INIVELNAL_DICI_M_26";

    private String format = "json";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getIpcSeriesId() {
        return ipcSeriesId;
    }

    public void setIpcSeriesId(String ipcSeriesId) {
        this.ipcSeriesId = ipcSeriesId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
