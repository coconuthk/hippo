package uk.nhs.digital.ps.chart.model;

@SuppressWarnings("PMD.UnusedPrivateField")
public class Titled {
    private final Title title;

    public Titled(String title) {
        this.title = new Title(title);
    }
}
