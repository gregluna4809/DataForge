CREATE TABLE dataset_cleaning_reports (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    cleaned_filename VARCHAR(255) NOT NULL,
    cleaned_storage_path VARCHAR(1000) NOT NULL,
    cleaned_file_size_bytes BIGINT NOT NULL,
    rows_read BIGINT NOT NULL,
    rows_written BIGINT NOT NULL,
    duplicate_rows_removed BIGINT NOT NULL,
    empty_rows_removed BIGINT NOT NULL,
    columns_renamed_json TEXT NOT NULL,
    cleaning_rules_applied_json TEXT NOT NULL,
    cleaned_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_dataset_cleaning_reports_dataset
        FOREIGN KEY (dataset_id)
        REFERENCES datasets (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_dataset_cleaning_reports_dataset
        UNIQUE (dataset_id)
);

CREATE INDEX idx_dataset_cleaning_reports_dataset_id
    ON dataset_cleaning_reports (dataset_id);
