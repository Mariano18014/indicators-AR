package com.indicadoresar.ingestion.indec;

import static org.assertj.core.api.Assertions.assertThat;

import com.indicadoresar.support.PostgresContainerSupport;
import com.indicadoresar.values.IndicatorValueRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.indicadoresar.TestBatchConfig;
import org.mockito.Mockito;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestBatchConfig.class)
@DirtiesContext
class IndecIpcJobIntegrationTest extends PostgresContainerSupport {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("indecIpcJob")
    private Job indecIpcJob;

    @Autowired
    private IndicatorValueRepository indicatorValueRepository;

    @MockitoBean
    private IndecClient indecClient;

    @BeforeEach
    void cleanDatabase() {
        cleanIndicatorValues();
    }

    private void cleanIndicatorValues() {
        indicatorValueRepository.deleteAll();
    }

    @Test
    void jobInsertsValueAndIsIdempotentOnSecondRun() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 1);
        IndecRate firstRate = new IndecRate(date, new BigDecimal("180.5000"), new BigDecimal("45.2000"));
        IndecRate secondRate = new IndecRate(date, new BigDecimal("182.0000"), new BigDecimal("46.0000"));

        Mockito.when(indecClient.fetchIpc()).thenReturn(firstRate).thenReturn(secondRate);

        jobLauncherTestUtils.setJob(indecIpcJob);

        JobExecution firstExecution = runJobWithUniqueParams(1);
        assertThat(firstExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(indicatorValueRepository.count()).isEqualTo(1);
        assertThat(findValueForDate(date)).isEqualByComparingTo(new BigDecimal("180.5000"));
        assertThat(findYoyForDate(date)).isEqualByComparingTo(new BigDecimal("45.2000"));

        JobExecution secondExecution = runJobWithUniqueParams(2);
        assertThat(secondExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(indicatorValueRepository.count()).isEqualTo(1);
        assertThat(findValueForDate(date)).isEqualByComparingTo(new BigDecimal("182.0000"));
        assertThat(findYoyForDate(date)).isEqualByComparingTo(new BigDecimal("46.0000"));
    }

    private JobExecution runJobWithUniqueParams(long runId) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", runId)
                .toJobParameters();
        return jobLauncherTestUtils.launchJob(params);
    }

    private BigDecimal findValueForDate(LocalDate date) {
        return indicatorValueRepository.findAll().stream()
                .filter(v -> v.getDate().equals(date))
                .findFirst()
                .orElseThrow()
                .getValue();
    }

    private BigDecimal findYoyForDate(LocalDate date) {
        return indicatorValueRepository.findAll().stream()
                .filter(v -> v.getDate().equals(date))
                .findFirst()
                .orElseThrow()
                .getYoyValue();
    }
}
