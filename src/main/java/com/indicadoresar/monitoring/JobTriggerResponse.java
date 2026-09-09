package com.indicadoresar.monitoring;

import org.springframework.batch.core.job.JobExecution;

public record JobTriggerResponse(String jobName, Long executionId, String status, String exitCode) {

    public static JobTriggerResponse fromJobExecution(String jobName, JobExecution execution) {
        return buildResponse(jobName, execution);
    }

    private static JobTriggerResponse buildResponse(String jobName, JobExecution execution) {
        String status = execution.getStatus().name();
        String exitCode = execution.getExitStatus().getExitCode();
        return new JobTriggerResponse(jobName, execution.getId(), status, exitCode);
    }
}
