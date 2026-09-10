package com.indicadoresar;

import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestBatchConfig {

    @Bean
    public JobOperatorTestUtils jobOperatorTestUtils(JobOperator jobOperator, JobRepository jobRepository) {
        return new JobOperatorTestUtils(jobOperator, jobRepository);
    }
}
