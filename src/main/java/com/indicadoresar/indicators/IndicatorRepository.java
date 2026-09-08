package com.indicadoresar.indicators;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorRepository extends JpaRepository<Indicator, Long> {

    Optional<Indicator> findByCode(String code);
}
