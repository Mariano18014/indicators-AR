package com.indicadoresar.dashboard;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        List<DashboardCard> cards = findDashboardCards();
        addCardsToModel(model, cards);
        return "dashboard";
    }

    private List<DashboardCard> findDashboardCards() {
        return dashboardService.findDashboardCards();
    }

    private void addCardsToModel(Model model, List<DashboardCard> cards) {
        model.addAttribute("cards", cards);
    }
}
