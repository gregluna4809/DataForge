package com.dataforge.cleaning;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatasetCsvCleanerTests {

    @TempDir
    private Path tempDir;

    private final DatasetCsvCleaner cleaner = new DatasetCsvCleaner();

    @Test
    void cleanTrimsValuesNormalizesHeadersRemovesEmptyAndDuplicateRows() throws Exception {
        Path source = tempDir.resolve("customers.csv");
        Files.writeString(source, """
                Customer ID, Full Name ,Signup Date
                  1 , Ada Lovelace , 2026-05-01
                , ,
                1,Ada Lovelace,2026-05-01
                2, Grace Hopper ,
                """);

        CleanedCsvResult result = cleaner.clean(source);

        assertThat(result.cleanedPath()).isNotEqualTo(source);
        assertThat(Files.exists(source)).isTrue();
        assertThat(Files.exists(result.cleanedPath())).isTrue();
        assertThat(result.rowsRead()).isEqualTo(4);
        assertThat(result.rowsWritten()).isEqualTo(2);
        assertThat(result.emptyRowsRemoved()).isEqualTo(1);
        assertThat(result.duplicateRowsRemoved()).isEqualTo(1);
        assertThat(result.columnsRenamed()).containsExactly(
                new ColumnRename("Customer ID", "customer_id"),
                new ColumnRename(" Full Name ", "full_name"),
                new ColumnRename("Signup Date", "signup_date")
        );
        assertThat(result.cleaningRulesApplied()).containsExactly(
                CleaningRule.TRIM_WHITESPACE,
                CleaningRule.NORMALIZE_BLANK_VALUES,
                CleaningRule.NORMALIZE_COLUMN_NAMES_TO_SNAKE_CASE,
                CleaningRule.REMOVE_FULLY_EMPTY_ROWS,
                CleaningRule.REMOVE_DUPLICATE_ROWS
        );

        List<String> cleanedLines = Files.readAllLines(result.cleanedPath());
        assertThat(cleanedLines).containsExactly(
                "customer_id,full_name,signup_date",
                "1,Ada Lovelace,2026-05-01",
                "2,Grace Hopper,"
        );
    }
}
