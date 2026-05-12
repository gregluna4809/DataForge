package com.dataforge.profiling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DatasetProfilerTests {

    private final DatasetProfiler profiler = new DatasetProfiler();

    @Test
    void profilesPreviewRowsPerColumn() {
        DatasetProfileResult result = profiler.profile(
                List.of("id", "status", "amount", "created_at", "notes"),
                List.of(
                        List.of("1", "true", "10.50", "2026-05-11", "alpha"),
                        List.of("2", "false", "10.50", "2026-05-12", "alpha"),
                        List.of("3", "true", "", "2026-05-13", "beta"),
                        List.of("", "true", "5.00", "", "")
                )
        );

        assertThat(result.columns()).hasSize(5);

        ColumnProfileResult id = result.columns().get(0);
        assertThat(id.nullCount()).isEqualTo(1);
        assertThat(id.nonNullCount()).isEqualTo(3);
        assertThat(id.uniqueCount()).isEqualTo(3);
        assertThat(id.inferredDataType()).isEqualTo(InferredDataType.INTEGER);

        ColumnProfileResult status = result.columns().get(1);
        assertThat(status.inferredDataType()).isEqualTo(InferredDataType.BOOLEAN);
        assertThat(status.mostCommonValues()).containsExactly(
                new MostCommonValue("true", 3),
                new MostCommonValue("false", 1)
        );

        ColumnProfileResult amount = result.columns().get(2);
        assertThat(amount.nullCount()).isEqualTo(1);
        assertThat(amount.inferredDataType()).isEqualTo(InferredDataType.DECIMAL);
        assertThat(amount.mostCommonValues().getFirst()).isEqualTo(new MostCommonValue("10.50", 2));

        ColumnProfileResult createdAt = result.columns().get(3);
        assertThat(createdAt.inferredDataType()).isEqualTo(InferredDataType.DATE);

        ColumnProfileResult notes = result.columns().get(4);
        assertThat(notes.inferredDataType()).isEqualTo(InferredDataType.TEXT);
        assertThat(notes.uniqueCount()).isEqualTo(2);
    }

    @Test
    void infersUnknownForAllNullColumn() {
        DatasetProfileResult result = profiler.profile(
                List.of("empty"),
                List.of(List.of(""), List.of("   "), List.of())
        );

        ColumnProfileResult column = result.columns().getFirst();
        assertThat(column.nullCount()).isEqualTo(3);
        assertThat(column.nonNullCount()).isZero();
        assertThat(column.uniqueCount()).isZero();
        assertThat(column.inferredDataType()).isEqualTo(InferredDataType.UNKNOWN);
        assertThat(column.mostCommonValues()).isEmpty();
    }
}
