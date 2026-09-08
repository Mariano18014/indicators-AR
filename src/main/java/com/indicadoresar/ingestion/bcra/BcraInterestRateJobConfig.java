package com.indicadoresar.ingestion.bcra;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BcraInterestRateJobConfig {

    public static final String JOB_NAME = "bcraInterestRateJob";
    public static final String STEP_NAME = "fetchAndStoreInterestRateStep";

    @Bean
    public Job bcraInterestRateJob(JobRepository jobRepository, Step fetchAndStoreInterestRateStep) {
        return buildJob(jobRepository, fetchAndStoreInterestRateStep);
    }

    private Job buildJob(JobRepository jobRepository, Step step) {
        return new JobBuilder(JOB_NAME, jobRepository).start(step).build();
    }

    @Bean
    public Step fetchAndStoreInterestRateStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BcraInterestRateTasklet tasklet) {
        return buildStep(jobRepository, transactionManager, tasklet);
    }

    private Step buildStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BcraInterestRateTasklet tasklet) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
