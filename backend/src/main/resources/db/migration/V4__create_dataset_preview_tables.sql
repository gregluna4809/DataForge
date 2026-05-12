CREATE TABLE dataset_columns (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    column_name TEXT NOT NULL,
    column_position INTEGER NOT NULL,
    CONSTRAINT fk_dataset_columns_dataset
        FOREIGN KEY (dataset_id)
        REFERENCES datasets (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_dataset_columns_dataset_position
        UNIQUE (dataset_id, column_position)
);

CREATE INDEX idx_dataset_columns_dataset_id ON dataset_columns (dataset_id);

CREATE TABLE dataset_preview_rows (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    row_position INTEGER NOT NULL,
    values_json TEXT NOT NULL,
    CONSTRAINT fk_dataset_preview_rows_dataset
        FOREIGN KEY (dataset_id)
        REFERENCES datasets (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_dataset_preview_rows_dataset_position
        UNIQUE (dataset_id, row_position)
);

CREATE INDEX idx_dataset_preview_rows_dataset_id ON dataset_preview_rows (dataset_id);
