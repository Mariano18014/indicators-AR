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
public class BcraExchangeRateTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(BcraExchangeRateTasklet.class);
    private static final String DOLAR_OFICIAL_CODE = "DOLAR_OFICIAL";

    private final BcraClient bcraClient;
    private final IndicatorRepository indicatorRepository;
    private final IndicatorValueRepository indicatorValueRepository;

    public BcraExchangeRateTasklet(
            BcraClient bcraClient,
            IndicatorRepository indicatorRepository,
            IndicatorValueRepository indicatorValueRepository) {
        this.bcraClient = bcraClient;
        this.indicatorRepository = indicatorRepository;
        this.indicatorValueRepository = indicatorValueRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Starting BCRA exchange rate ingestion");
        Indicator indicator = findIndicator();
        BcraExchangeRate exchangeRate = fetchExchangeRate();
        saveIndicatorValue(indicator, exchangeRate);
        log.info("Finished BCRA exchange rate ingestion for date {}", exchangeRate.date());
        return RepeatStatus.FINISHED;
    }

    private Indicator findIndicator() {
        return indicatorRepository
                .findByCode(DOLAR_OFICIAL_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found: " + DOLAR_OFICIAL_CODE));
    }

    private BcraExchangeRate fetchExchangeRate() {
        return bcraClient.fetchExchangeRate();
    }

    private void saveIndicatorValue(Indicator indicator, BcraExchangeRate exchangeRate) {
        Optional<IndicatorValue> existing = findExistingValue(indicator, exchangeRate);
        if (existing.isPresent()) {
            updateExistingValue(existing.get(), exchangeRate);
        } else {
            createNewValue(indicator, exchangeRate);
        }
    }

    private Optional<IndicatorValue> findExistingValue(
            Indicator indicator, BcraExchangeRate exchangeRate) {
        return indicatorValueRepository.findByIndicatorIdAndDate(
                indicator.getId(), exchangeRate.date());
    }

    private void updateExistingValue(IndicatorValue existing, BcraExchangeRate exchangeRate) {
        log.info(
                "Updating existing value for date {} from {} to {}",
                exchangeRate.date(),
                existing.getValue(),
                exchangeRate.value());
        existing.updateValue(exchangeRate.value(), Instant.now());
        indicatorValueRepository.save(existing);
    }

    private void createNewValue(Indicator indicator, BcraExchangeRate exchangeRate) {
        log.info("Creating new value for date {} value {}", exchangeRate.date(), exchangeRate.value());
        IndicatorValue newValue =
                new IndicatorValue(indicator, exchangeRate.date(), exchangeRate.value(), Instant.now());
        indicatorValueRepository.save(newValue);
    }
}
