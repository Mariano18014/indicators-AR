package com.indicadoresar.monitoring;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/monitoring")
public class MonitoringViewController {

    private final MonitoringService monitoringService;

    public MonitoringViewController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping
    public String showMonitoring(Model model) {
        List<JobStatusResponse> jobs = findAllJobsStatus();
        addJobsToModel(model, jobs);
        return "monitoring/jobs";
    }

    private List<JobStatusResponse> findAllJobsStatus() {
        return monitoringService.findAllJobsStatus();
    }

    private void addJobsToModel(Model model, List<JobStatusResponse> jobs) {
        model.addAttribute("jobs", jobs);
    }
}
