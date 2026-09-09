package com.indicadoresar.monitoring;

import com.indicadoresar.ingestion.bcra.BcraExchangeRateJobConfig;
import com.indicadoresar.ingestion.bcra.BcraInterestRateJobConfig;
import com.indicadoresar.ingestion.bcra.BcraReservesJobConfig;
import com.indicadoresar.ingestion.indec.IndecIpcJobConfig;
import java.util.List;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {

    private static final List<String> JOB_NAMES = List.of(
            BcraExchangeRateJobConfig.JOB_NAME,
            BcraInterestRateJobConfig.JOB_NAME,
            BcraReservesJobConfig.JOB_NAME,
            IndecIpcJobConfig.JOB_NAME);

    private final JobExplorer jobExplorer;

    public MonitoringService(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    public List<JobStatusResponse> findAllJobsStatus() {
        return buildAllStatuses();
    }

    private List<JobStatusResponse> buildAllStatuses() {
        return JOB_NAMES.stream().map(this::findLastStatusForJob).toList();
    }

    private JobStatusResponse findLastStatusForJob(String jobName) {
        JobExecution lastExecution = findLastExecution(jobName);
        if (lastExecution == null) {
            return JobStatusResponse.unknown(jobName);
        }
        return JobStatusResponse.fromJobExecution(jobName, lastExecution);
    }

    private JobExecution findLastExecution(String jobName) {
        var instances = jobExplorer.getJobInstances(jobName, 0, 1);
        if (instances.isEmpty()) {
            return null;
        }
        var executions = jobExplorer.getJobExecutions(instances.get(0));
        if (executions.isEmpty()) {
            return null;
        }
        return executions.get(0);
    }
}
