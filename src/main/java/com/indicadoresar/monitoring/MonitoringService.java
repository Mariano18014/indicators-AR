package com.indicadoresar.monitoring;

import com.indicadoresar.common.exception.ResourceNotFoundException;
import com.indicadoresar.ingestion.bcra.BcraExchangeRateJobConfig;
import com.indicadoresar.ingestion.bcra.BcraInterestRateJobConfig;
import com.indicadoresar.ingestion.bcra.BcraReservesJobConfig;
import com.indicadoresar.ingestion.indec.IndecIpcJobConfig;
import java.util.List;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {

    private static final List<String> JOB_NAMES = List.of(
            BcraExchangeRateJobConfig.JOB_NAME,
            BcraInterestRateJobConfig.JOB_NAME,
            BcraReservesJobConfig.JOB_NAME,
            IndecIpcJobConfig.JOB_NAME);

    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;
    private final ApplicationContext applicationContext;

    public MonitoringService(
            JobExplorer jobExplorer, JobLauncher jobLauncher, ApplicationContext applicationContext) {
        this.jobExplorer = jobExplorer;
        this.jobLauncher = jobLauncher;
        this.applicationContext = applicationContext;
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

    public JobTriggerResponse triggerJob(String jobName) {
        Job job = findJobByName(jobName);
        JobParameters params = buildUniqueJobParameters();
        JobExecution execution = launchJob(job, params);
        return JobTriggerResponse.fromJobExecution(jobName, execution);
    }

    private Job findJobByName(String jobName) {
        if (!JOB_NAMES.contains(jobName)) {
            throw new ResourceNotFoundException("Job not found: " + jobName);
        }
        try {
            return applicationContext.getBean(jobName, Job.class);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Job not found: " + jobName);
        }
    }

    private JobParameters buildUniqueJobParameters() {
        return new JobParametersBuilder().addLong("run.id", System.currentTimeMillis()).toJobParameters();
    }

    private JobExecution launchJob(Job job, JobParameters params) {
        try {
            return jobLauncher.run(job, params);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("already running") || e.getClass().getSimpleName().contains("AlreadyRunning")) {
                throw new IllegalStateException("Job already running: " + job.getName(), e);
            }
            if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            }
            throw new IllegalStateException("Failed to launch job: " + job.getName(), e);
        }
    }
}
