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
public class BcraInterestRateTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(BcraInterestRateTasklet.class);
    private static final String INTEREST_RATE_CODE = "TASA_POLITICA_MONETARIA";

    private final BcraClient bcraClient;
    private final IndicatorRepository indicatorRepository;
    private final IndicatorValueRepository indicatorValueRepository;

    public BcraInterestRateTasklet(
            BcraClient bcraClient,
            IndicatorRepository indicatorRepository,
            IndicatorValueRepository indicatorValueRepository) {
        this.bcraClient = bcraClient;
        this.indicatorRepository = indicatorRepository;
        this.indicatorValueRepository = indicatorValueRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Starting BCRA interest rate ingestion");
        Indicator indicator = findIndicator();
        BcraRate interestRate = fetchInterestRate();
        saveIndicatorValue(indicator, interestRate);
        log.info("Finished BCRA interest rate ingestion for date {}", interestRate.date());
        return RepeatStatus.FINISHED;
    }

    private Indicator findIndicator() {
        return indicatorRepository
                .findByCode(INTEREST_RATE_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found: " + INTEREST_RATE_CODE));
    }

    private BcraRate fetchInterestRate() {
        return bcraClient.fetchInterestRate();
    }

    private void saveIndicatorValue(Indicator indicator, BcraRate interestRate) {
        Optional<IndicatorValue> existing = findExistingValue(indicator, interestRate);
        if (existing.isPresent()) {
            updateExistingValue(existing.get(), interestRate);
        } else {
            createNewValue(indicator, interestRate);
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
