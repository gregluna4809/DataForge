package com.dataforge.profiling.dto;

import com.dataforge.profiling.InferredDataType;
import com.dataforge.profiling.MostCommonValue;
import java.util.List;

public record ColumnProfileResponse(
        String columnName,
        int columnPosition,
        long nullCount,
        long nonNullCount,
        long uniqueCount,
        InferredDataType inferredDataType,
        List<MostCommonValue> mostCommonValues
) {
}
