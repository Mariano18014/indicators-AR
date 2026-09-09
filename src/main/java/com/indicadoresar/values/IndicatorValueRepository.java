package com.indicadoresar.values;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorValueRepository extends JpaRepository<IndicatorValue, Long> {

    Optional<IndicatorValue> findByIndicatorIdAndDate(Long indicatorId, LocalDate date);

    List<IndicatorValue> findByIndicatorIdOrderByDateAsc(Long indicatorId);

    List<IndicatorValue> findByIndicatorIdAndDateBetweenOrderByDateAsc(
            Long indicatorId, LocalDate from, LocalDate to);

    List<IndicatorValue> findByIndicatorIdAndDateGreaterThanEqualOrderByDateAsc(
            Long indicatorId, LocalDate from);

    List<IndicatorValue> findByIndicatorIdAndDateLessThanEqualOrderByDateAsc(
            Long indicatorId, LocalDate to);

    Optional<IndicatorValue> findTopByIndicatorIdOrderByDateDesc(Long indicatorId);
}
