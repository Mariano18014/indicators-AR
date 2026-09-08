package com.indicadoresar.values;

import com.indicadoresar.indicators.Indicator;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndicatorValueService {

    private static final Logger log = LoggerFactory.getLogger(IndicatorValueService.class);

    private final IndicatorValueRepository indicatorValueRepository;

    public IndicatorValueService(IndicatorValueRepository indicatorValueRepository) {
        this.indicatorValueRepository = indicatorValueRepository;
    }

    @Transactional
    public IndicatorValue saveOrUpdate(Indicator indicator, LocalDate date, BigDecimal value) {
        return executeSaveOrUpdate(indicator, date, value);
    }

    private IndicatorValue executeSaveOrUpdate(Indicator indicator, LocalDate date, BigDecimal value) {
        Optional<IndicatorValue> existing = findExistingValue(indicator, date);
        if (existing.isPresent()) {
            return updateExistingValue(existing.get(), value);
        }
        return createNewValueWithRaceHandling(indicator, date, value);
    }

    private Optional<IndicatorValue> findExistingValue(Indicator indicator, LocalDate date) {
        return indicatorValueRepository.findByIndicatorIdAndDate(indicator.getId(), date);
    }

    private IndicatorValue updateExistingValue(IndicatorValue existing, BigDecimal value) {
        log.info(
                "Updating existing value for indicator {} date {} from {} to {}",
                existing.getIndicator().getCode(),
                existing.getDate(),
                existing.getValue(),
                value);
        existing.updateValue(value, Instant.now());
        return indicatorValueRepository.save(existing);
    }

    private IndicatorValue createNewValue(Indicator indicator, LocalDate date, BigDecimal value) {
        log.info("Creating new value for indicator {} date {} value {}", indicator.getCode(), date, value);
        IndicatorValue newValue = new IndicatorValue(indicator, date, value, Instant.now());
        return indicatorValueRepository.save(newValue);
    }

    private IndicatorValue createNewValueWithRaceHandling(
            Indicator indicator, LocalDate date, BigDecimal value) {
        try {
            return createNewValue(indicator, date, value);
        } catch (DataIntegrityViolationException ex) {
            return handleRaceCondition(indicator, date, value, ex);
        }
    }

    private IndicatorValue handleRaceCondition(
            Indicator indicator, LocalDate date, BigDecimal value, DataIntegrityViolationException ex) {
        log.warn(
                "Race condition detected for indicator {} date {}, retrying as update",
                indicator.getCode(),
                date,
                ex);
        Optional<IndicatorValue> existing = findExistingValue(indicator, date);
        if (existing.isPresent()) {
            return updateExistingValue(existing.get(), value);
        }
        throw ex;
    }
}
