package com.dataforge.datasets;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CsvPreviewParser {

    public static final int PREVIEW_ROW_LIMIT = 50;

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setTrim(false)
            .setIgnoreEmptyLines(false)
            .build();

    public CsvPreview parse(Path csvPath) {
        try (Reader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = CSV_FORMAT.parse(reader)) {
            List<CSVRecord> records = parser.stream()
                    .limit(PREVIEW_ROW_LIMIT + 1L)
                    .toList();

            if (records.isEmpty()) {
                throw new FileUploadException("CSV file must contain a header row");
            }

            List<String> columnNames = values(records.getFirst(), records.getFirst().size());
            if (columnNames.stream().allMatch(String::isBlank)) {
                throw new FileUploadException("CSV header row must contain at least one column name");
            }

            List<List<String>> rows = new ArrayList<>();
            for (int index = 1; index < records.size(); index++) {
                rows.add(values(records.get(index), columnNames.size()));
            }

            return new CsvPreview(columnNames, rows);
        } catch (IOException exception) {
            throw new FileUploadException("Failed to parse CSV preview", exception);
        } catch (IllegalArgumentException exception) {
            throw new FileUploadException("CSV file is malformed", exception);
        }
    }

    private List<String> values(CSVRecord record, int expectedSize) {
        List<String> values = new ArrayList<>(expectedSize);
        for (int index = 0; index < expectedSize; index++) {
            values.add(index < record.size() ? record.get(index) : "");
        }
        return values;
    }
}
