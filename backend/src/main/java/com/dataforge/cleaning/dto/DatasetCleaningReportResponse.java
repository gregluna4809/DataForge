package com.dataforge.cleaning.dto;

import com.dataforge.cleaning.CleaningRule;
import com.dataforge.cleaning.ColumnRename;
import com.dataforge.datasets.dto.DatasetResponse;
import java.time.Instant;
import java.util.List;

public record DatasetCleaningReportResponse(
        DatasetResponse dataset,
        String cleanedFilename,
        long cleanedFileSizeBytes,
        long rowsRead,
        long rowsWritten,
        long duplicateRowsRemoved,
        long emptyRowsRemoved,
        List<ColumnRename> columnsRenamed,
        List<CleaningRule> cleaningRulesApplied,
        Instant cleanedAt
) {
}
