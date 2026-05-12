package com.dataforge.datasets;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvPreviewParserTests {

    private final CsvPreviewParser parser = new CsvPreviewParser();

    @TempDir
    private Path tempDirectory;

    @Test
    void parsesHeaderAndQuotedPreviewRows() throws Exception {
        Path csv = tempDirectory.resolve("customers.csv");
        Files.writeString(csv, "id,name,notes\n"
                + "1,\"Ada Lovelace\",\"first, customer\"\n"
                + "2,\"Grace Hopper\",\"quoted \"\"value\"\"\"\n");

        CsvPreview preview = parser.parse(csv);

        assertThat(preview.columnNames()).containsExactly("id", "name", "notes");
        assertThat(preview.rows()).containsExactly(
                java.util.List.of("1", "Ada Lovelace", "first, customer"),
                java.util.List.of("2", "Grace Hopper", "quoted \"value\"")
        );
    }

    @Test
    void limitsPreviewToFirstFiftyDataRows() throws Exception {
        Path csv = tempDirectory.resolve("large.csv");
        StringBuilder content = new StringBuilder("id,name\n");
        IntStream.rangeClosed(1, 60)
                .forEach(index -> content.append(index).append(",Customer ").append(index).append('\n'));
        Files.writeString(csv, content);

        CsvPreview preview = parser.parse(csv);

        assertThat(preview.rows()).hasSize(50);
        assertThat(preview.rows().getFirst()).containsExactly("1", "Customer 1");
        assertThat(preview.rows().getLast()).containsExactly("50", "Customer 50");
    }

    @Test
    void padsMissingValuesToHeaderWidth() throws Exception {
        Path csv = tempDirectory.resolve("missing-values.csv");
        Files.writeString(csv, """
                id,name,email
                1,Ada
                """);

        CsvPreview preview = parser.parse(csv);

        assertThat(preview.rows()).containsExactly(java.util.List.of("1", "Ada", ""));
    }
}
