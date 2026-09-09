package com.indicadoresar.indicators;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndicatorService {

    private final IndicatorRepository indicatorRepository;

    public IndicatorService(IndicatorRepository indicatorRepository) {
        this.indicatorRepository = indicatorRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicatorResponse> findAllIndicators() {
        List<Indicator> indicators = findAllIndicatorsFromRepository();
        return buildResponses(indicators);
    }

    private List<Indicator> findAllIndicatorsFromRepository() {
        return indicatorRepository.findAllByOrderByCodeAsc();
    }

    private List<IndicatorResponse> buildResponses(List<Indicator> indicators) {
        return indicators.stream().map(IndicatorResponse::fromEntity).toList();
    }
}
