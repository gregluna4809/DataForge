package com.dataforge.cleaning;

import java.nio.file.Path;
import java.util.List;

public record CleanedCsvResult(
        Path cleanedPath,
        String cleanedFilename,
        long cleanedFileSizeBytes,
        long rowsRead,
        long rowsWritten,
        long duplicateRowsRemoved,
        long emptyRowsRemoved,
        List<ColumnRename> columnsRenamed,
        List<CleaningRule> cleaningRulesApplied
) {
}
