CREATE TABLE dataset_quality_scores (
    id UUID PRIMARY KEY,
    dataset_id UUID NOT NULL,
    overall_score DOUBLE PRECISION NOT NULL,
    issue_summaries_json TEXT NOT NULL,
    scored_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_dataset_quality_scores_dataset
        FOREIGN KEY (dataset_id)
        REFERENCES datasets (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_dataset_quality_scores_dataset
        UNIQUE (dataset_id)
);

CREATE INDEX idx_dataset_quality_scores_dataset_id ON dataset_quality_scores (dataset_id);

CREATE TABLE dataset_column_quality_scores (
    id UUID PRIMARY KEY,
    dataset_quality_score_id UUID NOT NULL,
    column_name TEXT NOT NULL,
    column_position INTEGER NOT NULL,
    quality_score DOUBLE PRECISION NOT NULL,
    null_percentage DOUBLE PRECISION NOT NULL,
    uniqueness_percentage DOUBLE PRECISION NOT NULL,
    empty_percentage DOUBLE PRECISION NOT NULL,
    type_consistency_score DOUBLE PRECISION NOT NULL,
    issue_summaries_json TEXT NOT NULL,
    CONSTRAINT fk_dataset_column_quality_scores_dataset_score
        FOREIGN KEY (dataset_quality_score_id)
        REFERENCES dataset_quality_scores (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_dataset_column_quality_scores_score_position
        UNIQUE (dataset_quality_score_id, column_position)
);

CREATE INDEX idx_dataset_column_quality_scores_dataset_score_id
    ON dataset_column_quality_scores (dataset_quality_score_id);
