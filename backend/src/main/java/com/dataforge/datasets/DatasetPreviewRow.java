package com.dataforge.datasets;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(
        name = "dataset_preview_rows",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_dataset_preview_rows_dataset_position",
                columnNames = {"dataset_id", "row_position"}
        )
)
public class DatasetPreviewRow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "row_position", nullable = false)
    private int position;

    @Column(name = "values_json", nullable = false, columnDefinition = "TEXT")
    private String valuesJson;

    protected DatasetPreviewRow() {
    }

    public DatasetPreviewRow(Dataset dataset, int position, String valuesJson) {
        this.dataset = dataset;
        this.position = position;
        this.valuesJson = valuesJson;
    }

    public int getPosition() {
        return position;
    }

    public String getValuesJson() {
        return valuesJson;
    }
}
