package uk.nhs.digital.common.components;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class FakeFacet {

    private String category;
    private String value;
    private String link;

    public FakeFacet(String category, String value, String link) {

        this.category = category;
        this.value = value;
        this.link = link;

    }

    public String getFacetCategory() {
        return category;
    }

    public String getFacetValue() {
        return value;
    }

    public String getFacetLink() {
        return link;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("category", getFacetCategory())
            .append("value", getFacetValue())
            .append("link", getFacetLink())
            .toString();
    }
}
