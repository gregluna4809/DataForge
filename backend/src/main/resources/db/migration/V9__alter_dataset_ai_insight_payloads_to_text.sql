ALTER TABLE dataset_ai_insights
    ALTER COLUMN dataset_description TYPE TEXT,
    ALTER COLUMN potential_issues_json TYPE TEXT,
    ALTER COLUMN suggested_analyses_json TYPE TEXT,
    ALTER COLUMN suggested_visualizations_json TYPE TEXT,
    ALTER COLUMN error_message TYPE TEXT;
