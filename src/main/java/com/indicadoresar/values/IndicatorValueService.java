package com.indicadoresar.values;

import com.indicadoresar.common.exception.ResourceNotFoundException;
import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
    private final IndicatorRepository indicatorRepository;

    public IndicatorValueService(
            IndicatorValueRepository indicatorValueRepository, IndicatorRepository indicatorRepository) {
        this.indicatorValueRepository = indicatorValueRepository;
        this.indicatorRepository = indicatorRepository;
    }

    @Transactional
    public IndicatorValue saveOrUpdate(Indicator indicator, LocalDate date, BigDecimal value) {
        return saveOrUpdate(indicator, date, value, null);
    }

    @Transactional
    public IndicatorValue saveOrUpdate(
            Indicator indicator, LocalDate date, BigDecimal value, BigDecimal yoyValue) {
        return executeSaveOrUpdate(indicator, date, value, yoyValue);
    }

    private IndicatorValue executeSaveOrUpdate(
            Indicator indicator, LocalDate date, BigDecimal value, BigDecimal yoyValue) {
        Optional<IndicatorValue> existing = findExistingValue(indicator, date);
        if (existing.isPresent()) {
            return updateExistingValue(existing.get(), value, yoyValue);
        }
        return createNewValueWithRaceHandling(indicator, date, value, yoyValue);
    }

    private IndicatorValue executeSaveOrUpdate(Indicator indicator, LocalDate date, BigDecimal value) {
        return executeSaveOrUpdate(indicator, date, value, null);
    }

    private Optional<IndicatorValue> findExistingValue(Indicator indicator, LocalDate date) {
        return indicatorValueRepository.findByIndicatorIdAndDate(indicator.getId(), date);
    }

    private IndicatorValue updateExistingValue(IndicatorValue existing, BigDecimal value) {
        return updateExistingValue(existing, value, null);
    }

    private IndicatorValue updateExistingValue(
            IndicatorValue existing, BigDecimal value, BigDecimal yoyValue) {
        log.info(
                "Updating existing value for indicator {} date {} from {} to {} yoy {} to {}",
                existing.getIndicator().getCode(),
                existing.getDate(),
                existing.getValue(),
                value,
                existing.getYoyValue(),
                yoyValue);
        existing.updateValue(value, yoyValue, Instant.now());
        return indicatorValueRepository.save(existing);
    }

    private IndicatorValue createNewValue(Indicator indicator, LocalDate date, BigDecimal value) {
        return createNewValue(indicator, date, value, null);
    }

    private IndicatorValue createNewValue(
            Indicator indicator, LocalDate date, BigDecimal value, BigDecimal yoyValue) {
        log.info(
                "Creating new value for indicator {} date {} value {} yoy {}",
                indicator.getCode(),
                date,
                value,
                yoyValue);
        IndicatorValue newValue = new IndicatorValue(indicator, date, value, yoyValue, Instant.now());
        return indicatorValueRepository.save(newValue);
    }

    private IndicatorValue createNewValueWithRaceHandling(
            Indicator indicator, LocalDate date, BigDecimal value) {
        return createNewValueWithRaceHandling(indicator, date, value, null);
    }

    private IndicatorValue createNewValueWithRaceHandling(
            Indicator indicator, LocalDate date, BigDecimal value, BigDecimal yoyValue) {
        try {
            return createNewValue(indicator, date, value, yoyValue);
        } catch (DataIntegrityViolationException ex) {
            return handleRaceCondition(indicator, date, value, yoyValue, ex);
        }
    }

    private IndicatorValue handleRaceCondition(
            Indicator indicator, LocalDate date, BigDecimal value, DataIntegrityViolationException ex) {
        return handleRaceCondition(indicator, date, value, null, ex);
    }

    private IndicatorValue handleRaceCondition(
            Indicator indicator,
            LocalDate date,
            BigDecimal value,
            BigDecimal yoyValue,
            DataIntegrityViolationException ex) {
        log.warn(
                "Race condition detected for indicator {} date {}, retrying as update",
                indicator.getCode(),
                date,
                ex);
        Optional<IndicatorValue> existing = findExistingValue(indicator, date);
        if (existing.isPresent()) {
            return updateExistingValue(existing.get(), value, yoyValue);
        }
        throw ex;
    }

    @Transactional(readOnly = true)
    public List<IndicatorValueResponse> findHistory(String code, LocalDate from, LocalDate to) {
        Indicator indicator = findIndicatorByCode(code);
        validateDateRange(from, to);
        List<IndicatorValue> values = findValuesByRange(indicator, from, to);
        return buildResponses(values);
    }

    private Indicator findIndicatorByCode(String code) {
        return indicatorRepository
                .findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found: " + code));
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
    }

    private List<IndicatorValue> findValuesByRange(Indicator indicator, LocalDate from, LocalDate to) {
        Long indicatorId = indicator.getId();
        if (from != null && to != null) {
            return indicatorValueRepository.findByIndicatorIdAndDateBetweenOrderByDateAsc(
                    indicatorId, from, to);
        }
        if (from != null) {
            return indicatorValueRepository.findByIndicatorIdAndDateGreaterThanEqualOrderByDateAsc(
                    indicatorId, from);
        }
        if (to != null) {
            return indicatorValueRepository.findByIndicatorIdAndDateLessThanEqualOrderByDateAsc(
                    indicatorId, to);
        }
        return indicatorValueRepository.findByIndicatorIdOrderByDateAsc(indicatorId);
    }

    private List<IndicatorValueResponse> buildResponses(List<IndicatorValue> values) {
        return values.stream().map(IndicatorValueResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public IndicatorValueResponse findLatest(String code) {
        Indicator indicator = findIndicatorByCode(code);
        IndicatorValue latest = findLatestValueForIndicator(indicator);
        return buildLatestResponse(latest);
    }

    private IndicatorValue findLatestValueForIndicator(Indicator indicator) {
        return indicatorValueRepository
                .findTopByIndicatorIdOrderByDateDesc(indicator.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("No values found for indicator: " + indicator.getCode()));
    }

    private IndicatorValueResponse buildLatestResponse(IndicatorValue value) {
        return IndicatorValueResponse.fromEntity(value);
    }
}
