CREATE TABLE dataset_ai_insights (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    generation_status VARCHAR(50) NOT NULL,
    model_name VARCHAR(120) NOT NULL,
    dataset_description TEXT NOT NULL,
    potential_issues_json TEXT NOT NULL,
    suggested_analyses_json TEXT NOT NULL,
    suggested_visualizations_json TEXT NOT NULL,
    error_message TEXT,
    generated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_dataset_ai_insights_dataset
        FOREIGN KEY (dataset_id)
        REFERENCES datasets (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_dataset_ai_insights_dataset
        UNIQUE (dataset_id)
);

CREATE INDEX idx_dataset_ai_insights_dataset_id ON dataset_ai_insights (dataset_id);
