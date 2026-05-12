CREATE TABLE dataset_column_profiles (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    column_name TEXT NOT NULL,
    column_position INTEGER NOT NULL,
    null_count BIGINT NOT NULL,
    non_null_count BIGINT NOT NULL,
    unique_count BIGINT NOT NULL,
    inferred_data_type VARCHAR(50) NOT NULL,
    most_common_values_json TEXT NOT NULL,
    profiled_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_dataset_column_profiles_dataset
        FOREIGN KEY (dataset_id)
        REFERENCES datasets (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_dataset_column_profiles_dataset_position
        UNIQUE (dataset_id, column_position)
);

CREATE INDEX idx_dataset_column_profiles_dataset_id ON dataset_column_profiles (dataset_id);
