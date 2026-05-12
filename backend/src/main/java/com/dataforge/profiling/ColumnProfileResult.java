package com.dataforge.profiling;

import java.util.List;

public record ColumnProfileResult(
        String columnName,
        int columnPosition,
        long nullCount,
        long nonNullCount,
        long uniqueCount,
        InferredDataType inferredDataType,
        List<MostCommonValue> mostCommonValues
) {
}
