package com.indicadoresar.monitoring;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/jobs/{jobName}/run")
    public String triggerJob(@PathVariable String jobName, RedirectAttributes redirectAttributes) {
        JobTriggerResponse response = triggerJobByName(jobName);
        addFlashMessage(redirectAttributes, response);
        return "redirect:/monitoring";
    }

    private List<JobStatusResponse> findAllJobsStatus() {
        return monitoringService.findAllJobsStatus();
    }

    private void addJobsToModel(Model model, List<JobStatusResponse> jobs) {
        model.addAttribute("jobs", jobs);
    }

    private JobTriggerResponse triggerJobByName(String jobName) {
        return monitoringService.triggerJob(jobName);
    }

    private void addFlashMessage(RedirectAttributes redirectAttributes, JobTriggerResponse response) {
        redirectAttributes.addFlashAttribute(
                "message", "Job " + response.jobName() + " launched with status " + response.status());
    }
}
