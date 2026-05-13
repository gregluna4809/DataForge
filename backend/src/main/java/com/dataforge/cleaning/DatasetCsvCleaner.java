package com.dataforge.cleaning;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class DatasetCsvCleaner {

    private static final String CSV_EXTENSION = ".csv";
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setTrim(false)
            .setIgnoreEmptyLines(false)
            .build();
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern REPEATED_UNDERSCORES = Pattern.compile("_+");

    public CleanedCsvResult clean(Path sourcePath) {
        Path normalizedSourcePath = sourcePath.toAbsolutePath().normalize();
        validateSource(normalizedSourcePath);

        Path outputPath = cleanedOutputPath(normalizedSourcePath);
        try (Reader reader = Files.newBufferedReader(normalizedSourcePath, StandardCharsets.UTF_8);
             CSVParser parser = CSV_FORMAT.parse(reader);
             BufferedWriter writer = Files.newBufferedWriter(
                     outputPath,
                     StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE_NEW,
                     StandardOpenOption.WRITE
             );
             CSVPrinter printer = new CSVPrinter(writer, CSV_FORMAT)) {

            List<CSVRecord> records = parser.stream().toList();
            if (records.isEmpty()) {
                throw new DatasetCleaningException("Uploaded CSV must contain a header row before cleaning");
            }

            List<String> originalHeaders = values(records.getFirst(), records.getFirst().size());
            List<String> cleanedHeaders = normalizeHeaders(originalHeaders);
            List<ColumnRename> columnRenames = columnRenames(originalHeaders, cleanedHeaders);
            printer.printRecord(cleanedHeaders);

            long rowsRead = 0;
            long rowsWritten = 0;
            long emptyRowsRemoved = 0;
            long duplicateRowsRemoved = 0;
            Set<List<String>> seenRows = new HashSet<>();

            for (int index = 1; index < records.size(); index++) {
                rowsRead++;
                List<String> cleanedRow = normalizeRow(records.get(index), cleanedHeaders.size());
                if (isEmptyRow(cleanedRow)) {
                    emptyRowsRemoved++;
                    continue;
                }

                if (!seenRows.add(cleanedRow)) {
                    duplicateRowsRemoved++;
                    continue;
                }

                printer.printRecord(cleanedRow);
                rowsWritten++;
            }

            printer.flush();

            return new CleanedCsvResult(
                    outputPath,
                    outputPath.getFileName().toString(),
                    Files.size(outputPath),
                    rowsRead,
                    rowsWritten,
                    duplicateRowsRemoved,
                    emptyRowsRemoved,
                    columnRenames,
                    List.of(
                            CleaningRule.TRIM_WHITESPACE,
                            CleaningRule.NORMALIZE_BLANK_VALUES,
                            CleaningRule.NORMALIZE_COLUMN_NAMES_TO_SNAKE_CASE,
                            CleaningRule.REMOVE_FULLY_EMPTY_ROWS,
                            CleaningRule.REMOVE_DUPLICATE_ROWS
                    )
            );
        } catch (IOException exception) {
            throw new DatasetCleaningException("Failed to clean CSV file", exception);
        } catch (IllegalArgumentException exception) {
            throw new DatasetCleaningException("CSV file is malformed and could not be cleaned", exception);
        }
    }

    private void validateSource(Path sourcePath) {
        if (!Files.exists(sourcePath)) {
            throw new DatasetCleaningException("Original uploaded CSV file was not found");
        }

        if (!Files.isRegularFile(sourcePath)) {
            throw new DatasetCleaningException("Original uploaded CSV path is not a file");
        }
    }

    private Path cleanedOutputPath(Path sourcePath) {
        Path parent = sourcePath.getParent();
        if (parent == null) {
            throw new DatasetCleaningException("Original uploaded CSV file must have a storage directory");
        }

        String sourceFilename = sourcePath.getFileName().toString();
        String baseName = sourceFilename.toLowerCase(Locale.ROOT).endsWith(CSV_EXTENSION)
                ? sourceFilename.substring(0, sourceFilename.length() - CSV_EXTENSION.length())
                : sourceFilename;
        return parent.resolve(baseName + "-cleaned-" + UUID.randomUUID() + CSV_EXTENSION).normalize();
    }

    private List<String> normalizeHeaders(List<String> originalHeaders) {
        List<String> normalizedHeaders = new ArrayList<>(originalHeaders.size());
        Set<String> usedHeaders = new HashSet<>();
        for (int index = 0; index < originalHeaders.size(); index++) {
            String normalized = normalizeHeader(originalHeaders.get(index));
            if (normalized.isBlank()) {
                normalized = "column_" + (index + 1);
            }

            String unique = normalized;
            int suffix = 2;
            while (!usedHeaders.add(unique)) {
                unique = normalized + "_" + suffix;
                suffix++;
            }
            normalizedHeaders.add(unique);
        }
        return normalizedHeaders;
    }

    private String normalizeHeader(String header) {
        String value = header == null ? "" : header.trim().toLowerCase(Locale.ROOT);
        value = NON_ALPHANUMERIC.matcher(value).replaceAll("_");
        value = REPEATED_UNDERSCORES.matcher(value).replaceAll("_");
        value = trimUnderscores(value);
        return value;
    }

    private String trimUnderscores(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '_') {
            start++;
        }
        while (end > start && value.charAt(end - 1) == '_') {
            end--;
        }
        return value.substring(start, end);
    }

    private List<ColumnRename> columnRenames(List<String> originalHeaders, List<String> cleanedHeaders) {
        List<ColumnRename> renames = new ArrayList<>();
        for (int index = 0; index < originalHeaders.size(); index++) {
            String original = originalHeaders.get(index);
            String cleaned = cleanedHeaders.get(index);
            if (!cleaned.equals(original)) {
                renames.add(new ColumnRename(original, cleaned));
            }
        }
        return renames;
    }

    private List<String> normalizeRow(CSVRecord record, int expectedSize) {
        List<String> row = new ArrayList<>(expectedSize);
        for (int index = 0; index < expectedSize; index++) {
            String value = index < record.size() ? record.get(index) : "";
            row.add(value == null ? "" : value.trim());
        }
        return row;
    }

    private List<String> values(CSVRecord record, int expectedSize) {
        List<String> values = new ArrayList<>(expectedSize);
        for (int index = 0; index < expectedSize; index++) {
            values.add(index < record.size() ? record.get(index) : "");
        }
        return values;
    }

    private boolean isEmptyRow(List<String> row) {
        return row.stream().allMatch(String::isBlank);
    }
}
