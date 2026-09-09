package com.indicadoresar.dashboard;

import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.values.IndicatorValueResponse;
import com.indicadoresar.values.IndicatorValueService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final IndicatorRepository indicatorRepository;
    private final IndicatorValueService indicatorValueService;

    public DashboardService(
            IndicatorRepository indicatorRepository, IndicatorValueService indicatorValueService) {
        this.indicatorRepository = indicatorRepository;
        this.indicatorValueService = indicatorValueService;
    }

    public List<DashboardCard> findDashboardCards() {
        List<Indicator> indicators = findAllIndicators();
        return buildCards(indicators);
    }

    private List<Indicator> findAllIndicators() {
        return indicatorRepository.findAllByOrderByCodeAsc();
    }

    private List<DashboardCard> buildCards(List<Indicator> indicators) {
        return indicators.stream().map(this::buildCard).toList();
    }

    private DashboardCard buildCard(Indicator indicator) {
        Optional<IndicatorValueResponse> latest = findLatestValueForIndicator(indicator);
        return DashboardCard.fromIndicatorAndValue(indicator, latest);
    }

    private Optional<IndicatorValueResponse> findLatestValueForIndicator(Indicator indicator) {
        try {
            IndicatorValueResponse latest = indicatorValueService.findLatest(indicator.getCode());
            return Optional.of(latest);
        } catch (com.indicadoresar.common.exception.ResourceNotFoundException e) {
            return Optional.empty();
        }
    }
}
