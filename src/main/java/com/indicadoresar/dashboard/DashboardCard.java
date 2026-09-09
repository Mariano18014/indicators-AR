package com.indicadoresar.dashboard;

import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.values.IndicatorValueResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public record DashboardCard(
        String code,
        String name,
        String unit,
        String frequency,
        BigDecimal value,
        LocalDate date,
        BigDecimal yoyValue,
        boolean hasData) {

    public static DashboardCard fromIndicatorAndValue(
            Indicator indicator, Optional<IndicatorValueResponse> latest) {
        return buildCard(indicator, latest);
    }

    private static DashboardCard buildCard(
            Indicator indicator, Optional<IndicatorValueResponse> latest) {
        if (latest.isPresent()) {
            return buildCardWithData(indicator, latest.get());
        }
        return buildCardWithoutData(indicator);
    }

    private static DashboardCard buildCardWithData(
            Indicator indicator, IndicatorValueResponse value) {
        return new DashboardCard(
                indicator.getCode(),
                indicator.getName(),
                indicator.getUnit(),
                indicator.getFrequency().name(),
                value.value(),
                value.date(),
                value.yoyValue(),
                true);
    }

    private static DashboardCard buildCardWithoutData(Indicator indicator) {
        return new DashboardCard(
                indicator.getCode(),
                indicator.getName(),
                indicator.getUnit(),
                indicator.getFrequency().name(),
                null,
                null,
                null,
                false);
    }
}
