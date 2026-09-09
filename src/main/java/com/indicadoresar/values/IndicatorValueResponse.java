package com.indicadoresar.values;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndicatorValueResponse(LocalDate date, BigDecimal value, BigDecimal yoyValue) {

    public static IndicatorValueResponse fromEntity(IndicatorValue entity) {
        return new IndicatorValueResponse(entity.getDate(), entity.getValue(), entity.getYoyValue());
    }
}
