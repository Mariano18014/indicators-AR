package com.indicadoresar.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.indicadoresar.ingestion.bcra.BcraClient;
import com.indicadoresar.ingestion.bcra.BcraRate;
import com.indicadoresar.ingestion.indec.IndecClient;
import com.indicadoresar.ingestion.indec.IndecRate;
import com.indicadoresar.support.PostgresContainerSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.Mockito;
import com.indicadoresar.TestBatchConfig;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestBatchConfig.class)
@DirtiesContext
class MonitoringServiceTest extends PostgresContainerSupport {

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("bcraExchangeRateJob")
    private Job bcraExchangeRateJob;

    @MockitoBean
    private BcraClient bcraClient;

    @MockitoBean
    private IndecClient indecClient;

    @BeforeEach
    void setUp() {
        Mockito.when(indecClient.fetchIpc())
                .thenReturn(new IndecRate(LocalDate.of(2026, 8, 1), new BigDecimal("180.50"), null));
    }

    @Test
    void findAllJobsStatusReturnsUnknownWhenNoExecution() {
        var statuses = monitoringService.findAllJobsStatus();

        assertThat(statuses).hasSize(4);
        assertThat(statuses).allSatisfy(s -> assertThat(s.lastStatus()).isEqualTo("UNKNOWN"));
        assertThat(statuses).allSatisfy(s -> assertThat(s.lastExecutionTime()).isNull());
    }

    @Test
    void findAllJobsStatusReturnsCompletedAfterSuccess() throws Exception {
        BcraRate rate = new BcraRate(LocalDate.of(2026, 9, 7), new BigDecimal("1210.75"));
        Mockito.when(bcraClient.fetchExchangeRate()).thenReturn(rate);

        jobLauncherTestUtils.setJob(bcraExchangeRateJob);
        var execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addLong("run.id", 1L).toJobParameters());
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        var statuses = monitoringService.findAllJobsStatus();
        var dolarStatus = findByJobName(statuses, "bcraExchangeRateJob");

        assertThat(dolarStatus.lastStatus()).isEqualTo("COMPLETED");
        assertThat(dolarStatus.lastExecutionTime()).isNotNull();
        assertThat(dolarStatus.lastExitCode()).isEqualTo("COMPLETED");
    }

    @Test
    void findAllJobsStatusReturnsFailedAfterFailure() throws Exception {
        Mockito.when(bcraClient.fetchExchangeRate()).thenThrow(new IllegalStateException("BCRA down"));

        jobLauncherTestUtils.setJob(bcraExchangeRateJob);
        var execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addLong("run.id", 2L).toJobParameters());
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);

        var statuses = monitoringService.findAllJobsStatus();
        var dolarStatus = findByJobName(statuses, "bcraExchangeRateJob");

        assertThat(dolarStatus.lastStatus()).isEqualTo("FAILED");
        assertThat(dolarStatus.lastExecutionTime()).isNotNull();
    }

    private JobStatusResponse findByJobName(java.util.List<JobStatusResponse> statuses, String jobName) {
        return statuses.stream()
                .filter(s -> s.jobName().equals(jobName))
                .findFirst()
                .orElseThrow();
    }
}
