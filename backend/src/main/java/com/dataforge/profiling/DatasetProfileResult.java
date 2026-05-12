package com.dataforge.profiling;

import java.util.List;

public record DatasetProfileResult(
        List<ColumnProfileResult> columns
) {
}
