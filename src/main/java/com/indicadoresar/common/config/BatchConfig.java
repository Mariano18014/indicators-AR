package com.indicadoresar.common.config;

import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.context.annotation.Configuration;

// Spring Batch 6 defaults to an in-memory ResourcelessJobRepository (tracks only the last
// launched job); MonitoringService needs per-job history, hence the JDBC-backed override.
@Configuration
public class BatchConfig extends JdbcDefaultBatchConfiguration {}
