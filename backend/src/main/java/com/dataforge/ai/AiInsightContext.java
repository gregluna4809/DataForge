package com.dataforge.ai;

import com.dataforge.datasets.Dataset;
import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.quality.DatasetQualityScore;
import java.util.List;

public record AiInsightContext(
        Dataset dataset,
        List<String> columnNames,
        List<List<String>> previewRows,
        List<DatasetColumnProfile> profiles,
        DatasetQualityScore qualityScore
) {
}
