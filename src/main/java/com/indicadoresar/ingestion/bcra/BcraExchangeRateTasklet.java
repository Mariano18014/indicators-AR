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
public class BcraExchangeRateTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(BcraExchangeRateTasklet.class);
    private static final String DOLAR_OFICIAL_CODE = "DOLAR_OFICIAL";

    private final BcraClient bcraClient;
    private final IndicatorRepository indicatorRepository;
    private final IndicatorValueService indicatorValueService;
    private final RetryTemplate retryTemplate;

    public BcraExchangeRateTasklet(
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
        log.info("Starting BCRA exchange rate ingestion");
        return executeWithRetry();
    }

    private RepeatStatus executeWithRetry() {
        return retryTemplate.execute(
                context -> {
                    log.info("Attempt {} for BCRA exchange rate ingestion", context.getRetryCount() + 1);
                    Indicator indicator = findIndicator();
                    BcraRate exchangeRate = fetchExchangeRate();
                    saveIndicatorValue(indicator, exchangeRate);
                    log.info("Finished BCRA exchange rate ingestion for date {}", exchangeRate.date());
                    return RepeatStatus.FINISHED;
                });
    }

    private Indicator findIndicator() {
        return indicatorRepository
                .findByCode(DOLAR_OFICIAL_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found: " + DOLAR_OFICIAL_CODE));
    }

    private BcraRate fetchExchangeRate() {
        return bcraClient.fetchExchangeRate();
    }

    private void saveIndicatorValue(Indicator indicator, BcraRate exchangeRate) {
        indicatorValueService.saveOrUpdate(indicator, exchangeRate.date(), exchangeRate.value());
    }
}
