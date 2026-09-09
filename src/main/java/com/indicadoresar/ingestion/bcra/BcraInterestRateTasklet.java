package com.indicadoresar.ingestion.bcra;

import com.indicadoresar.common.exception.ResourceNotFoundException;
import com.indicadoresar.indicators.Indicator;
import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.values.IndicatorValueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
public class BcraInterestRateTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(BcraInterestRateTasklet.class);
    private static final String INTEREST_RATE_CODE = "TASA_POLITICA_MONETARIA";

    private final BcraClient bcraClient;
    private final IndicatorRepository indicatorRepository;
    private final IndicatorValueService indicatorValueService;
    private final RetryTemplate retryTemplate;

    public BcraInterestRateTasklet(
            BcraClient bcraClient,
            IndicatorRepository indicatorRepository,
            IndicatorValueService indicatorValueService,
            RetryTemplate retryTemplate) {
        this.bcraClient = bcraClient;
        this.indicatorRepository = indicatorRepository;
        this.indicatorValueService = indicatorValueService;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Starting BCRA interest rate ingestion");
        return executeWithRetry();
    }

    private RepeatStatus executeWithRetry() {
        return retryTemplate.execute(
                context -> {
                    log.info("Attempt {} for BCRA interest rate ingestion", context.getRetryCount() + 1);
                    Indicator indicator = findIndicator();
                    BcraRate interestRate = fetchInterestRate();
                    saveIndicatorValue(indicator, interestRate);
                    log.info("Finished BCRA interest rate ingestion for date {}", interestRate.date());
                    return RepeatStatus.FINISHED;
                });
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
        indicatorValueService.saveOrUpdate(indicator, interestRate.date(), interestRate.value());
    }
}
