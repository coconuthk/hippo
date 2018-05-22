package uk.nhs.digital.common;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.apache.jackrabbit.value.BooleanValue;
import org.apache.jackrabbit.value.DateValue;
import org.apache.jackrabbit.value.LongValue;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.jcr.Value;

@RunWith(DataProviderRunner.class)
public class OrderedSearchDateDerivedDataFunctionTest {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private static final String ORDERED_SEARCH_DATE = "orderedSearchDate";

    private OrderedSearchDateDerivedDataFunction function = new OrderedSearchDateDerivedDataFunction();

    @Test
    public void testUpcoming() throws Exception {
        Map<String, Value[]> result = testFunctionCompute("20-03-2020", false);

        long expectedValue = -parseDate("20-03-2020").getTimeInMillis();

        assertThat("Result is as expected", result, hasEntry(ORDERED_SEARCH_DATE, new Value[]{new LongValue(expectedValue)}));
    }

    @Test
    public void testPublished() throws Exception {
        Map<String, Value[]> result = testFunctionCompute("10-08-2010", true);

        long expectedValue = parseDate("10-08-2010").getTimeInMillis();

        assertThat("Result is as expected", result, hasEntry(ORDERED_SEARCH_DATE, new Value[]{new LongValue(expectedValue)}));
    }

    @Test
    public void testNoAccessibleFlag() throws Exception {
        Map<String, Value[]> result = testFunctionCompute("18-11-2013", null);

        long expectedValue = parseDate("18-11-2013").getTimeInMillis();

        assertThat("Result is as expected", result, hasEntry(ORDERED_SEARCH_DATE, new Value[]{new LongValue(expectedValue)}));
    }

    @Test
    public void testNoDate() throws Exception {
        assertNull(testFunctionCompute(null, true).get(ORDERED_SEARCH_DATE));

        assertNull(testFunctionCompute(null, false).get(ORDERED_SEARCH_DATE));

        assertNull(testFunctionCompute(null, null).get(ORDERED_SEARCH_DATE));
    }

    @Test
    public void testOrder() throws Exception {
        Map<String, Long> inputToOutput = new HashMap();
        addData(inputToOutput, "25-02-2020", null);
        addData(inputToOutput, "09-03-2020", true);
        addData(inputToOutput, "10-03-2020", false);
        addData(inputToOutput, "11-03-2020", true);
        addData(inputToOutput, "12-03-2020", false);
        addData(inputToOutput, "15-03-2020", null);
        addData(inputToOutput, "20-05-2023", true);
        addData(inputToOutput, "21-06-2023", false);

        List<String> sortedDates = inputToOutput.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        // We sort from highest to lowest so need to reverse
        Collections.reverse(sortedDates);

        // The order should be published first in descending order followed by upcoming in ascending date order
        assertThat("Order is as expected", sortedDates, contains(
            "20-05-2023",
            "15-03-2020",
            "11-03-2020",
            "09-03-2020",
            "25-02-2020",
            "10-03-2020",
            "12-03-2020",
            "21-06-2023"
        ));
    }

    private void addData(Map<String, Long> inputToOutput, String dateStr, Boolean publiclyAccessible) throws Exception {
        Map<String, Value[]> result = testFunctionCompute(dateStr, publiclyAccessible);
        inputToOutput.put(dateStr, result.get(ORDERED_SEARCH_DATE)[0].getLong());
    }

    private Map<String, Value[]> testFunctionCompute(String dateStr, Boolean publiclyAccessible) throws Exception {
        Map<String, Value[]> map = new HashMap<>();

        if (dateStr != null) {
            map.put("date", new Value[]{new DateValue(parseDate(dateStr))});
        }

        if (publiclyAccessible != null) {
            map.put("publiclyAccessible", new Value[]{new BooleanValue(publiclyAccessible)});
        }

        return function.compute(map);
    }

    private Calendar parseDate(String dateStr) throws ParseException {
        Calendar date = Calendar.getInstance();
        date.setTime(DATE_FORMAT.parse(dateStr));
        return date;
    }
}
