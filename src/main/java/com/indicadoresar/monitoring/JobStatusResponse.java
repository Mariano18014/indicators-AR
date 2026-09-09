package com.indicadoresar.monitoring;

import java.time.LocalDateTime;
import org.springframework.batch.core.job.JobExecution;

public record JobStatusResponse(
        String jobName, String lastStatus, LocalDateTime lastExecutionTime, String lastExitCode, String lastExitDescription) {

    public static JobStatusResponse fromJobExecution(String jobName, JobExecution execution) {
        return buildResponse(jobName, execution);
    }

    public static JobStatusResponse unknown(String jobName) {
        return new JobStatusResponse(jobName, "UNKNOWN", null, null, null);
    }

    private static JobStatusResponse buildResponse(String jobName, JobExecution execution) {
        String status = execution.getStatus().name();
        LocalDateTime time = extractExecutionTime(execution);
        String exitCode = execution.getExitStatus().getExitCode();
        String exitDescription = execution.getExitStatus().getExitDescription();
        return new JobStatusResponse(jobName, status, time, exitCode, exitDescription);
    }

    private static LocalDateTime extractExecutionTime(JobExecution execution) {
        if (execution.getEndTime() != null) {
            return execution.getEndTime();
        }
        if (execution.getCreateTime() != null) {
            return execution.getCreateTime();
        }
        return null;
    }
}
