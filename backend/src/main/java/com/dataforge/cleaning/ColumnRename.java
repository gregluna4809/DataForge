package com.dataforge.cleaning;

public record ColumnRename(
        String originalName,
        String cleanedName
) {
}
