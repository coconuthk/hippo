package uk.nhs.digital.ps.chart;

import static java.util.Collections.singletonList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.nhs.digital.ps.chart.model.Point;
import uk.nhs.digital.ps.chart.model.Series;

import java.io.InputStream;
import java.util.*;
import javax.jcr.Binary;

public class ChartFactory {
    private static final int CATEGORIES_INDEX = 0;

    private final ChartType type;
    private final String title;
    private final String yTitle;
    private final Binary data;

    private List<String> categories;
    private HashMap<Integer, Series> series;

    public ChartFactory(String type, String title, String yTitle, Binary data) {
        this.type = ChartType.toChartType(type);
        this.title = title;
        this.yTitle = yTitle;
        this.data = data;
    }

    public SeriesChart build() {
        try {
            parse();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse chart data file", ex);
        }

        switch (type) {
            case PIE:
                // We only have one series in a pie chart so just get the first
                return new SeriesChart(type, title, singletonList(getSeries().get(0)), null, null);
            case BAR:
            case COLUMN:
            case LINE:
            case STACKED_BAR:
            case STACKED_COLUMN:
                return new SeriesChart(type, title, getSeries(), yTitle, getCategories());
            default:
                throw new RuntimeException("Unknown Chart Type: " + type);
        }
    }

    private void parse() throws Exception {
        categories = new ArrayList<>();
        series = new HashMap<>();

        XSSFWorkbook workbook;
        try (InputStream is = data.getStream()) {
            workbook = new XSSFWorkbook(is);
        }

        XSSFSheet sheet = workbook.getSheetAt(0);

        // Get the headers
        Iterator<Row> rowIterator = sheet.rowIterator();
        Row header = rowIterator.next();
        series = new HashMap<>();
        for (int i = 1; i < header.getLastCellNum(); i++) {
            Cell cell = header.getCell(i);
            series.put(i, new Series(cell.getStringCellValue()));
        }

        // Get the data
        rowIterator.forEachRemaining(row -> {
            // First column is the series name (category)
            String category = getStringValue(row.getCell(CATEGORIES_INDEX));
            categories.add(category);

            for (int i = 1; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                series.computeIfAbsent(i, key -> new Series(""))
                    .add(new Point(category, getDoubleValue(cell)));
            }
        });
    }

    private static Double getDoubleValue(Cell cell) {
        return cell.getCellType() == Cell.CELL_TYPE_STRING ? Double.valueOf(cell.getStringCellValue()) : cell.getNumericCellValue();
    }

    private static String getStringValue(Cell cell) {
        return cell.getCellType() == Cell.CELL_TYPE_STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue());
    }

    private List<String> getCategories() {
        return categories;
    }

    private List<Series> getSeries() {
        return new ArrayList<>(series.values());
    }

}
