package com.indicadoresar.common.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Configuration
public class RetryConfig {

    private final RetryProperties properties;

    public RetryConfig(RetryProperties properties) {
        this.properties = properties;
    }

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
                        HttpClientErrorException.class, true,
                        ResourceAccessException.class, true);
        return new SimpleRetryPolicy(properties.getMaxAttempts(), retryableExceptions, true);
    }

    private ExponentialBackOffPolicy buildBackOffPolicy() {
        ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(properties.getBackoff().getInitialInterval());
        policy.setMultiplier(properties.getBackoff().getMultiplier());
        policy.setMaxInterval(properties.getBackoff().getMaxInterval());
        return policy;
    }
}
