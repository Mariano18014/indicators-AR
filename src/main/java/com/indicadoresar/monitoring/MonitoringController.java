package com.indicadoresar.monitoring;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobStatusResponse>> getJobsStatus() {
        List<JobStatusResponse> statuses = findAllJobsStatus();
        return ResponseEntity.ok(statuses);
    }

    @PostMapping("/jobs/{jobName}/run")
    public ResponseEntity<JobTriggerResponse> triggerJob(@PathVariable String jobName) {
        JobTriggerResponse response = triggerJobByName(jobName);
        return ResponseEntity.ok(response);
    }

    private List<JobStatusResponse> findAllJobsStatus() {
        return monitoringService.findAllJobsStatus();
    }

    private JobTriggerResponse triggerJobByName(String jobName) {
        return monitoringService.triggerJob(jobName);
    }
}
