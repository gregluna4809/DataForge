package com.dataforge.cleaning;

import com.dataforge.datasets.Dataset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dataset_cleaning_reports")
public class DatasetCleaningReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "cleaned_filename", nullable = false, length = 255)
    private String cleanedFilename;

    @Column(name = "cleaned_storage_path", nullable = false, length = 1000)
    private String cleanedStoragePath;

    @Column(name = "cleaned_file_size_bytes", nullable = false)
    private long cleanedFileSizeBytes;

    @Column(name = "rows_read", nullable = false)
    private long rowsRead;

    @Column(name = "rows_written", nullable = false)
    private long rowsWritten;

    @Column(name = "duplicate_rows_removed", nullable = false)
    private long duplicateRowsRemoved;

    @Column(name = "empty_rows_removed", nullable = false)
    private long emptyRowsRemoved;

    @Column(name = "columns_renamed_json", nullable = false)
    private String columnsRenamedJson;

    @Column(name = "cleaning_rules_applied_json", nullable = false)
    private String cleaningRulesAppliedJson;

    @Column(name = "cleaned_at", nullable = false)
    private Instant cleanedAt;

    protected DatasetCleaningReport() {
    }

    public DatasetCleaningReport(
            Dataset dataset,
            String cleanedFilename,
            String cleanedStoragePath,
            long cleanedFileSizeBytes,
            long rowsRead,
            long rowsWritten,
            long duplicateRowsRemoved,
            long emptyRowsRemoved,
            String columnsRenamedJson,
            String cleaningRulesAppliedJson,
            Instant cleanedAt
    ) {
        this.dataset = dataset;
        this.cleanedFilename = cleanedFilename;
        this.cleanedStoragePath = cleanedStoragePath;
        this.cleanedFileSizeBytes = cleanedFileSizeBytes;
        this.rowsRead = rowsRead;
        this.rowsWritten = rowsWritten;
        this.duplicateRowsRemoved = duplicateRowsRemoved;
        this.emptyRowsRemoved = emptyRowsRemoved;
        this.columnsRenamedJson = columnsRenamedJson;
        this.cleaningRulesAppliedJson = cleaningRulesAppliedJson;
        this.cleanedAt = cleanedAt;
    }

    public UUID getId() {
        return id;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public String getCleanedFilename() {
        return cleanedFilename;
    }

    public String getCleanedStoragePath() {
        return cleanedStoragePath;
    }

    public long getCleanedFileSizeBytes() {
        return cleanedFileSizeBytes;
    }

    public long getRowsRead() {
        return rowsRead;
    }

    public long getRowsWritten() {
        return rowsWritten;
    }

    public long getDuplicateRowsRemoved() {
        return duplicateRowsRemoved;
    }

    public long getEmptyRowsRemoved() {
        return emptyRowsRemoved;
    }

    public String getColumnsRenamedJson() {
        return columnsRenamedJson;
    }

    public String getCleaningRulesAppliedJson() {
        return cleaningRulesAppliedJson;
    }

    public Instant getCleanedAt() {
        return cleanedAt;
    }
}
