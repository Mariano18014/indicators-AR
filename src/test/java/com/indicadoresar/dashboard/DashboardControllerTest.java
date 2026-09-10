package com.indicadoresar.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.indicadoresar.indicators.IndicatorRepository;
import com.indicadoresar.support.PostgresContainerSupport;
import com.indicadoresar.values.IndicatorValueRepository;
import com.indicadoresar.values.IndicatorValueService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class DashboardControllerTest extends PostgresContainerSupport {

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Autowired
    private IndicatorValueRepository indicatorValueRepository;

    @Autowired
    private IndicatorValueService indicatorValueService;

    @BeforeEach
    void setUp() {
        indicatorValueRepository.deleteAll();
    }

    @Test
    void showDashboardReturnsDashboardView() {
        Model model = new ConcurrentModel();

        String view = dashboardController.showDashboard(model);

        assertThat(view).isEqualTo("dashboard");
    }

    @Test
    void showDashboardAddsCardsToModel() {
        seedOneValuePerIndicator();
        Model model = new ConcurrentModel();

        dashboardController.showDashboard(model);

        assertThat(model.containsAttribute("cards")).isTrue();
        var cards = findCardsFromModel(model);
        assertThat(cards).hasSize(4);
    }

    @Test
    void dashboardCardsAreOrderedByCodeAsc() {
        seedOneValuePerIndicator();
        Model model = new ConcurrentModel();

        dashboardController.showDashboard(model);

        var cards = findCardsFromModel(model);
        assertThat(cards.get(0).code()).isEqualTo("DOLAR_OFICIAL");
        assertThat(cards.get(1).code()).isEqualTo("IPC_NACIONAL");
    }

    @Test
    void dashboardCardsHaveDataWhenValuesExist() {
        seedOneValuePerIndicator();
        Model model = new ConcurrentModel();

        dashboardController.showDashboard(model);

        var cards = findCardsFromModel(model);
        assertThat(cards).allSatisfy(card -> assertThat(card.hasData()).isTrue());
    }

    @Test
    void dashboardCardsShowNoDataWhenNoValues() {
        Model model = new ConcurrentModel();

        dashboardController.showDashboard(model);

        var cards = findCardsFromModel(model);
        assertThat(cards).allSatisfy(card -> assertThat(card.hasData()).isFalse());
        assertThat(cards).allSatisfy(card -> assertThat(card.value()).isNull());
    }

    @Test
    void dashboardServiceReturnsCardsOrderedByCodeAsc() {
        seedOneValuePerIndicator();

        var cards = dashboardService.findDashboardCards();

        assertThat(cards).hasSize(4);
        assertThat(cards.get(0).code()).isEqualTo("DOLAR_OFICIAL");
    }

    @SuppressWarnings("unchecked")
    private List<DashboardCard> findCardsFromModel(Model model) {
        return (List<DashboardCard>) model.getAttribute("cards");
    }

    private void seedOneValuePerIndicator() {
        var indicators = indicatorRepository.findAllByOrderByCodeAsc();
        for (var indicator : indicators) {
            indicatorValueService.saveOrUpdate(indicator, LocalDate.of(2026, 9, 7), new BigDecimal("100.00"));
        }
    }
}
