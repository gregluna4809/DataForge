package com.dataforge.profiling;

import com.dataforge.datasets.Dataset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "dataset_column_profiles",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_dataset_column_profiles_dataset_position",
                columnNames = {"dataset_id", "column_position"}
        )
)
public class DatasetColumnProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "column_name", nullable = false, columnDefinition = "TEXT")
    private String columnName;

    @Column(name = "column_position", nullable = false)
    private int columnPosition;

    @Column(name = "null_count", nullable = false)
    private long nullCount;

    @Column(name = "non_null_count", nullable = false)
    private long nonNullCount;

    @Column(name = "unique_count", nullable = false)
    private long uniqueCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "inferred_data_type", nullable = false, length = 50)
    private InferredDataType inferredDataType;

    @Column(name = "most_common_values_json", nullable = false, columnDefinition = "TEXT")
    private String mostCommonValuesJson;

    @Column(name = "profiled_at", nullable = false)
    private Instant profiledAt;

    protected DatasetColumnProfile() {
    }

    public DatasetColumnProfile(
            Dataset dataset,
            ColumnProfileResult result,
            String mostCommonValuesJson,
            Instant profiledAt
    ) {
        this.dataset = dataset;
        this.columnName = result.columnName();
        this.columnPosition = result.columnPosition();
        this.nullCount = result.nullCount();
        this.nonNullCount = result.nonNullCount();
        this.uniqueCount = result.uniqueCount();
        this.inferredDataType = result.inferredDataType();
        this.mostCommonValuesJson = mostCommonValuesJson;
        this.profiledAt = profiledAt;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getColumnPosition() {
        return columnPosition;
    }

    public long getNullCount() {
        return nullCount;
    }

    public long getNonNullCount() {
        return nonNullCount;
    }

    public long getUniqueCount() {
        return uniqueCount;
    }

    public InferredDataType getInferredDataType() {
        return inferredDataType;
    }

    public String getMostCommonValuesJson() {
        return mostCommonValuesJson;
    }

    public Instant getProfiledAt() {
        return profiledAt;
    }
}
