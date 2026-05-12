package com.dataforge.profiling;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DatasetProfiler {

    private static final int MOST_COMMON_VALUE_LIMIT = 5;

    public DatasetProfileResult profile(List<String> columnNames, List<List<String>> rows) {
        List<ColumnProfileResult> columnProfiles = new ArrayList<>(columnNames.size());

        for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
            columnProfiles.add(profileColumn(columnNames.get(columnIndex), columnIndex, rows));
        }

        return new DatasetProfileResult(columnProfiles);
    }

    private ColumnProfileResult profileColumn(String columnName, int columnIndex, List<List<String>> rows) {
        long nullCount = 0;
        long nonNullCount = 0;
        Set<String> uniqueValues = new HashSet<>();
        Map<String, ValueFrequency> frequencies = new LinkedHashMap<>();
        List<String> nonNullValues = new ArrayList<>();

        for (List<String> row : rows) {
            String rawValue = columnIndex < row.size() ? row.get(columnIndex) : "";
            String normalizedValue = rawValue == null ? "" : rawValue.trim();

            if (normalizedValue.isEmpty()) {
                nullCount++;
                continue;
            }

            nonNullCount++;
            uniqueValues.add(normalizedValue);
            nonNullValues.add(normalizedValue);
            frequencies.computeIfAbsent(normalizedValue, value -> new ValueFrequency(value, frequencies.size()))
                    .increment();
        }

        return new ColumnProfileResult(
                columnName,
                columnIndex,
                nullCount,
                nonNullCount,
                uniqueValues.size(),
                inferDataType(nonNullValues),
                mostCommonValues(frequencies)
        );
    }

    private InferredDataType inferDataType(List<String> values) {
        if (values.isEmpty()) {
            return InferredDataType.UNKNOWN;
        }

        if (values.stream().allMatch(this::isBoolean)) {
            return InferredDataType.BOOLEAN;
        }

        if (values.stream().allMatch(this::isInteger)) {
            return InferredDataType.INTEGER;
        }

        if (values.stream().allMatch(this::isDecimal)) {
            return InferredDataType.DECIMAL;
        }

        if (values.stream().allMatch(this::isDate)) {
            return InferredDataType.DATE;
        }

        if (values.stream().allMatch(this::isDateTime)) {
            return InferredDataType.DATETIME;
        }

        return InferredDataType.TEXT;
    }

    private boolean isBoolean(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.equals("true") || normalized.equals("false");
    }

    private boolean isInteger(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private boolean isDecimal(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private boolean isDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    private boolean isDateTime(String value) {
        try {
            OffsetDateTime.parse(value);
            return true;
        } catch (DateTimeParseException exception) {
            try {
                Instant.parse(value);
                return true;
            } catch (DateTimeParseException nestedException) {
                return false;
            }
        }
    }

    private List<MostCommonValue> mostCommonValues(Map<String, ValueFrequency> frequencies) {
        return frequencies.values()
                .stream()
                .sorted(Comparator.comparingLong(ValueFrequency::count).reversed()
                        .thenComparingInt(ValueFrequency::firstSeenIndex))
                .limit(MOST_COMMON_VALUE_LIMIT)
                .map(frequency -> new MostCommonValue(frequency.value(), frequency.count()))
                .toList();
    }

    private static final class ValueFrequency {
        private final String value;
        private final int firstSeenIndex;
        private long count;

        private ValueFrequency(String value, int firstSeenIndex) {
            this.value = value;
            this.firstSeenIndex = firstSeenIndex;
        }

        private void increment() {
            count++;
        }

        private String value() {
            return value;
        }

        private int firstSeenIndex() {
            return firstSeenIndex;
        }

        private long count() {
            return count;
        }
    }
}
