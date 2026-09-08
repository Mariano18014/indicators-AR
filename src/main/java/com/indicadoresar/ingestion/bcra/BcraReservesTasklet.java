package com.indicadoresar.ingestion.bcra;

import com.indicadoresar.common.exception.ResourceNotFoundException;
import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.values.IndicatorValue;
import com.indicadoresar.values.IndicatorValueRepository;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class BcraReservesTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(BcraReservesTasklet.class);
    private static final String RESERVES_CODE = "RESERVAS_INTERNACIONALES";

    private final BcraClient bcraClient;
    private final IndicatorRepository indicatorRepository;
    private final IndicatorValueRepository indicatorValueRepository;

    public BcraReservesTasklet(
            BcraClient bcraClient,
            IndicatorRepository indicatorRepository,
            IndicatorValueRepository indicatorValueRepository) {
        this.bcraClient = bcraClient;
        this.indicatorRepository = indicatorRepository;
        this.indicatorValueRepository = indicatorValueRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Starting BCRA reserves ingestion");
        Indicator indicator = findIndicator();
        BcraRate reserves = fetchReserves();
        saveIndicatorValue(indicator, reserves);
        log.info("Finished BCRA reserves ingestion for date {}", reserves.date());
        return RepeatStatus.FINISHED;
    }

    private Indicator findIndicator() {
        return indicatorRepository
                .findByCode(RESERVES_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found: " + RESERVES_CODE));
    }

    private BcraRate fetchReserves() {
        return bcraClient.fetchReserves();
    }

    private void saveIndicatorValue(Indicator indicator, BcraRate reserves) {
        Optional<IndicatorValue> existing = findExistingValue(indicator, reserves);
        if (existing.isPresent()) {
            updateExistingValue(existing.get(), reserves);
        } else {
            createNewValue(indicator, reserves);
        }
    }

    private Optional<IndicatorValue> findExistingValue(Indicator indicator, BcraRate rate) {
        return indicatorValueRepository.findByIndicatorIdAndDate(indicator.getId(), rate.date());
    }

    private void updateExistingValue(IndicatorValue existing, BcraRate rate) {
        log.info(
                "Updating existing value for date {} from {} to {}",
                rate.date(),
                existing.getValue(),
                rate.value());
        existing.updateValue(rate.value(), Instant.now());
        indicatorValueRepository.save(existing);
    }

    private void createNewValue(Indicator indicator, BcraRate rate) {
        log.info("Creating new value for date {} value {}", rate.date(), rate.value());
        IndicatorValue newValue = new IndicatorValue(indicator, rate.date(), rate.value(), Instant.now());
        indicatorValueRepository.save(newValue);
    }
}
