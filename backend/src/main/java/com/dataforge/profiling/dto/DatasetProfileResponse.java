package com.dataforge.profiling.dto;

import com.dataforge.datasets.dto.DatasetResponse;
import java.util.List;

public record DatasetProfileResponse(
        DatasetResponse dataset,
        List<ColumnProfileResponse> columns
) {
}
