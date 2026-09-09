package com.indicadoresar.indicators;

public record IndicatorResponse(
        String code, String name, String unit, String source, String frequency, String description) {

    public static IndicatorResponse fromEntity(Indicator indicator) {
        return new IndicatorResponse(
                indicator.getCode(),
                indicator.getName(),
                indicator.getUnit(),
                indicator.getSource().name(),
                indicator.getFrequency().name(),
                indicator.getDescription());
    }
}
