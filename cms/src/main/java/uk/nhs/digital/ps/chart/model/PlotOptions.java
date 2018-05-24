package uk.nhs.digital.ps.chart.model;

@SuppressWarnings("PMD.UnusedPrivateField")
public class PlotOptions {
    private SeriesOptions series;

    public PlotOptions(String stackingType) {
        this.series = new SeriesOptions(stackingType);
    }
}
