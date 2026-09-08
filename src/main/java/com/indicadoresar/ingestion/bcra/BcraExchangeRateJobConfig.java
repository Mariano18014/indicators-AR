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
public class BcraExchangeRateJobConfig {

    public static final String JOB_NAME = "bcraExchangeRateJob";
    public static final String STEP_NAME = "fetchAndStoreExchangeRateStep";

    @Bean
    public Job bcraExchangeRateJob(JobRepository jobRepository, Step fetchAndStoreExchangeRateStep) {
        return buildJob(jobRepository, fetchAndStoreExchangeRateStep);
    }

    private Job buildJob(JobRepository jobRepository, Step step) {
        return new JobBuilder(JOB_NAME, jobRepository).start(step).build();
    }

    @Bean
    public Step fetchAndStoreExchangeRateStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BcraExchangeRateTasklet tasklet) {
        return buildStep(jobRepository, transactionManager, tasklet);
    }

    private Step buildStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BcraExchangeRateTasklet tasklet) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
