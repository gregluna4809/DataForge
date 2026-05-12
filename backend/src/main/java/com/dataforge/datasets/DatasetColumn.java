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
        name = "dataset_columns",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_dataset_columns_dataset_position",
                columnNames = {"dataset_id", "column_position"}
        )
)
public class DatasetColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "column_name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "column_position", nullable = false)
    private int position;

    protected DatasetColumn() {
    }

    public DatasetColumn(Dataset dataset, String name, int position) {
        this.dataset = dataset;
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
}
