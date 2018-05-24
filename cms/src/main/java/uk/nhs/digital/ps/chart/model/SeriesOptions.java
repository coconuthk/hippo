package uk.nhs.digital.ps.chart.model;

@SuppressWarnings("PMD.UnusedPrivateField")
public class SeriesOptions {
    private final String stacking;

    public SeriesOptions(String stackingType) {
        this.stacking = stackingType;
    }
}
