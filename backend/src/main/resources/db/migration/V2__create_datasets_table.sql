CREATE TABLE datasets (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    upload_timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    row_count BIGINT NOT NULL DEFAULT 0,
    column_count INTEGER NOT NULL DEFAULT 0,
    file_size_bytes BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    uploaded_by_user_id UUID NOT NULL,
    CONSTRAINT fk_datasets_uploaded_by
        FOREIGN KEY (uploaded_by_user_id)
        REFERENCES users (id)
);

CREATE INDEX idx_datasets_uploaded_by_user_id ON datasets (uploaded_by_user_id);
CREATE INDEX idx_datasets_uploaded_by_timestamp ON datasets (uploaded_by_user_id, upload_timestamp DESC);
