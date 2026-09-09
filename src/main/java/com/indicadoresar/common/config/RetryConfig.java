package com.indicadoresar.common.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Configuration
public class RetryConfig {

    @Value("${batch.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${batch.retry.backoff.initial-interval:1000}")
    private long initialInterval;

    @Value("${batch.retry.backoff.multiplier:2.0}")
    private double multiplier;

    @Value("${batch.retry.backoff.max-interval:5000}")
    private long maxInterval;

    @Bean
    public RetryTemplate retryTemplate() {
        return buildRetryTemplate();
    }

    private RetryTemplate buildRetryTemplate() {
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(buildRetryPolicy());
        template.setBackOffPolicy(buildBackOffPolicy());
        return template;
    }

    private SimpleRetryPolicy buildRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions =
                Map.of(
                        IllegalStateException.class, true,
                        HttpServerErrorException.class, true,
                        ResourceAccessException.class, true);
        return new SimpleRetryPolicy(maxAttempts, retryableExceptions, true);
    }

    private ExponentialBackOffPolicy buildBackOffPolicy() {
        ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(initialInterval);
        policy.setMultiplier(multiplier);
        policy.setMaxInterval(maxInterval);
        return policy;
    }
}
