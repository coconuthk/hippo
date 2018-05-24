package uk.nhs.digital.ps.chart.model;

import uk.nhs.digital.ps.chart.ChartType;

@SuppressWarnings("PMD.UnusedPrivateField")
public class Chart {
    private final String type;

    public Chart(ChartType type) {
        this.type = type.getHighChartsType();
    }
}
