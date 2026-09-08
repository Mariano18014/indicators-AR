package com.indicadoresar;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestBatchConfig {

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils(
            JobLauncher jobLauncher, JobRepository jobRepository) {
        JobLauncherTestUtils utils = new JobLauncherTestUtils();
        utils.setJobLauncher(jobLauncher);
        utils.setJobRepository(jobRepository);
        return utils;
    }
}
