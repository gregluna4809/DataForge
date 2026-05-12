package com.dataforge.datasets;

import java.util.List;

public record CsvPreview(
        List<String> columnNames,
        List<List<String>> rows
) {
}
