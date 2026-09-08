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
import org.springframework.stereotype.Component;

@Component
public class BcraReservesTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(BcraReservesTasklet.class);
    private static final String RESERVES_CODE = "RESERVAS_INTERNACIONALES";

    private final BcraClient bcraClient;
    private final IndicatorRepository indicatorRepository;
    private final IndicatorValueService indicatorValueService;

    public BcraReservesTasklet(
            BcraClient bcraClient,
            IndicatorRepository indicatorRepository,
            IndicatorValueService indicatorValueService) {
        this.bcraClient = bcraClient;
        this.indicatorRepository = indicatorRepository;
        this.indicatorValueService = indicatorValueService;
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
        indicatorValueService.saveOrUpdate(indicator, reserves.date(), reserves.value());
    }
}
