package uk.nhs.digital.ps;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.hippoecm.repository.ext.DerivedDataFunction;
import uk.nhs.digital.ps.chart.ChartFactory;
import uk.nhs.digital.ps.chart.SeriesChart;

import java.util.Map;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

public class HighChartsConfigDerivedDataFunction extends DerivedDataFunction {

    private static final long serialVersionUID = 1;

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    @Override
    public Map<String, Value[]> compute(Map<String, Value[]> parameters) {
        new RuntimeException("Creating DerData").printStackTrace();
        try {
            // required fields
            Value dataValue = getValue(parameters, "data");
            Value typeValue = getValue(parameters, "type");
            Value titleValue = getValue(parameters, "title");

            if (!allNotNull(dataValue, typeValue, titleValue)) {
                return emptyMap();
            }

            // optional field
            Value yTitleValue = getValue(parameters, "yTitle");
            String yTitle = yTitleValue == null ? null : yTitleValue.getString();

            SeriesChart chart = new ChartFactory(typeValue.getString(), titleValue.getString(), yTitle, dataValue.getBinary()).build();

            String chartConfig = OBJECT_MAPPER.writeValueAsString(chart);

            parameters.put("chartConfig", new Value[]{getValueFactory().createValue(chartConfig)});
        } catch (JsonProcessingException | RepositoryException ex) {
            throw new RuntimeException("Failed to create chart config derived data", ex);
        }

        return parameters;
    }

    private Value getValue(Map<String, Value[]> parameters, String parameterName) {
        Value[] values = parameters.get(parameterName);
        return ArrayUtils.isEmpty(values) ? null : values[0];
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }
}
