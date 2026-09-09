package com.indicadoresar.ingestion.indec;

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
public class IndecIpcTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(IndecIpcTasklet.class);
    private static final String IPC_CODE = "IPC_NACIONAL";

    private final IndecClient indecClient;
    private final IndicatorRepository indicatorRepository;
    private final IndicatorValueService indicatorValueService;
    private final RetryTemplate retryTemplate;

    public IndecIpcTasklet(
            IndecClient indecClient,
            IndicatorRepository indicatorRepository,
            IndicatorValueService indicatorValueService,
            RetryTemplate retryTemplate) {
        this.indecClient = indecClient;
        this.indicatorRepository = indicatorRepository;
        this.indicatorValueService = indicatorValueService;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Starting INDEC IPC ingestion");
        return executeWithRetry();
    }

    private RepeatStatus executeWithRetry() {
        return retryTemplate.execute(
                context -> {
                    log.info("Attempt {} for INDEC IPC ingestion", context.getRetryCount() + 1);
                    Indicator indicator = findIndicator();
                    IndecRate ipc = fetchIpc();
                    saveIndicatorValue(indicator, ipc);
                    log.info("Finished INDEC IPC ingestion for date {}", ipc.date());
                    return RepeatStatus.FINISHED;
                });
    }

    private Indicator findIndicator() {
        return indicatorRepository
                .findByCode(IPC_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found: " + IPC_CODE));
    }

    private IndecRate fetchIpc() {
        return indecClient.fetchIpc();
    }

    private void saveIndicatorValue(Indicator indicator, IndecRate ipc) {
        indicatorValueService.saveOrUpdate(indicator, ipc.date(), ipc.value(), ipc.yoyValue());
    }
}
