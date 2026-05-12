ALTER TABLE datasets
    ADD COLUMN stored_filename VARCHAR(255),
    ADD COLUMN storage_path VARCHAR(1000),
    ADD COLUMN uploaded_file_content_type VARCHAR(100),
    ADD COLUMN file_uploaded_at TIMESTAMPTZ;

CREATE INDEX idx_datasets_stored_filename ON datasets (stored_filename);
