package com.indicadoresar.ingestion.indec;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class IndecIpcJobConfig {

    public static final String JOB_NAME = "indecIpcJob";
    public static final String STEP_NAME = "fetchAndStoreIpcStep";

    @Bean
    public Job indecIpcJob(JobRepository jobRepository, Step fetchAndStoreIpcStep) {
        return buildJob(jobRepository, fetchAndStoreIpcStep);
    }

    private Job buildJob(JobRepository jobRepository, Step step) {
        return new JobBuilder(JOB_NAME, jobRepository).start(step).build();
    }

    @Bean
    public Step fetchAndStoreIpcStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            IndecIpcTasklet tasklet) {
        return buildStep(jobRepository, transactionManager, tasklet);
    }

    private Step buildStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            IndecIpcTasklet tasklet) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
