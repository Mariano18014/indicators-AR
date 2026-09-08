package com.indicadoresar.values;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorValueRepository extends JpaRepository<IndicatorValue, Long> {

    Optional<IndicatorValue> findByIndicatorIdAndDate(Long indicatorId, LocalDate date);
}
