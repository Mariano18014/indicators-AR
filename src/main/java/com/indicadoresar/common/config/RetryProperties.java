package com.indicadoresar.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.retry")
public class RetryProperties {

    private int maxAttempts = 3;

    private Backoff backoff = new Backoff();

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Backoff getBackoff() {
        return backoff;
    }

    public void setBackoff(Backoff backoff) {
        this.backoff = backoff;
    }

    public static class Backoff {

        private long initialInterval = 1000;

        private double multiplier = 2.0;

        private long maxInterval = 5000;

        public long getInitialInterval() {
            return initialInterval;
        }

        public void setInitialInterval(long initialInterval) {
            this.initialInterval = initialInterval;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public long getMaxInterval() {
            return maxInterval;
        }

        public void setMaxInterval(long maxInterval) {
            this.maxInterval = maxInterval;
        }
    }
}
