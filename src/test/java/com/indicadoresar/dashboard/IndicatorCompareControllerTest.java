package com.indicadoresar.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.indicadoresar.support.PostgresContainerSupport;
import java.time.LocalDate;
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
class IndicatorCompareControllerTest extends PostgresContainerSupport {

    @Autowired
    private IndicatorCompareController compareController;

    @Test
    void showCompareReturnsCompareView() {
        Model model = new ConcurrentModel();

        String view = compareController.showCompare("DOLAR_OFICIAL", "IPC_NACIONAL", null, null, model);

        assertThat(view).isEqualTo("indicator/compare");
    }

    @Test
    void showCompareAddsIndicatorsAndCodesToModel() {
        Model model = new ConcurrentModel();

        compareController.showCompare("DOLAR_OFICIAL", "IPC_NACIONAL", null, null, model);

        assertThat(model.containsAttribute("indicators")).isTrue();
        assertThat(model.getAttribute("code1")).isEqualTo("DOLAR_OFICIAL");
        assertThat(model.getAttribute("code2")).isEqualTo("IPC_NACIONAL");
    }

    @Test
    void showCompareUsesDefaultsWhenCodesNull() {
        Model model = new ConcurrentModel();

        compareController.showCompare(null, null, null, null, model);

        assertThat(model.getAttribute("code1")).isEqualTo("DOLAR_OFICIAL");
        assertThat(model.getAttribute("code2")).isEqualTo("IPC_NACIONAL");
    }

    @Test
    void showCompareThrowsWhenCodesSame() {
        Model model = new ConcurrentModel();

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> compareController.showCompare("DOLAR_OFICIAL", "DOLAR_OFICIAL", null, null, model));
    }

    @Test
    void showCompareResolvesRangeWithDefaults() {
        Model model = new ConcurrentModel();

        compareController.showCompare("DOLAR_OFICIAL", "IPC_NACIONAL", null, null, model);

        assertThat(model.getAttribute("from")).isInstanceOf(LocalDate.class);
        assertThat(model.getAttribute("to")).isInstanceOf(LocalDate.class);
    }

    @Test
    void showCompareWithExplicitRangeKeepsIt() {
        Model model = new ConcurrentModel();
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 9, 7);

        compareController.showCompare("DOLAR_OFICIAL", "IPC_NACIONAL", from, to, model);

        assertThat(model.getAttribute("from")).isEqualTo(from);
        assertThat(model.getAttribute("to")).isEqualTo(to);
    }
}
