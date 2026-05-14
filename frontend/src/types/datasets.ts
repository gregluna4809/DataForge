export type DatasetStatus = "METADATA_CREATED" | "UPLOADED" | "PROCESSING" | "READY" | "FAILED";

export type UploadedBy = {
  id: string;
  email: string;
  name: string;
};

export type Dataset = {
  id: string;
  name: string;
  originalFilename: string;
  description: string | null;
  uploadTimestamp: string;
  rowCount: number;
  columnCount: number;
  fileSizeBytes: number;
  storedFilename: string | null;
  fileUploadedAt: string | null;
  status: DatasetStatus;
  uploadedBy: UploadedBy;
};

export type CreateDatasetRequest = {
  name: string;
  originalFilename: string;
  description: string | null;
  rowCount: number;
  columnCount: number;
  fileSizeBytes: number;
};

export type DatasetUploadResponse = {
  datasetId: string;
  originalFilename: string;
  storedFilename: string;
  fileSizeBytes: number;
  contentType: string | null;
  status: DatasetStatus;
  uploadedAt: string;
};

export type DatasetPreviewResponse = {
  dataset: Dataset;
  columnNames: string[];
  rows: string[][];
};

export type InferredDataType = "UNKNOWN" | "BOOLEAN" | "INTEGER" | "DECIMAL" | "DATE" | "DATETIME" | "TEXT";

export type MostCommonValue = {
  value: string;
  count: number;
};

export type ColumnProfile = {
  columnName: string;
  columnPosition: number;
  nullCount: number;
  nonNullCount: number;
  uniqueCount: number;
  inferredDataType: InferredDataType;
  mostCommonValues: MostCommonValue[];
};

export type DatasetProfileResponse = {
  dataset: Dataset;
  columns: ColumnProfile[];
};

export type QualityIssueType =
  | "HIGH_NULL_RATE"
  | "POSSIBLE_IDENTIFIER_COLUMN"
  | "LOW_UNIQUENESS"
  | "EMPTY_COLUMN"
  | "INFERRED_TEXT_TYPE"
  | "UNKNOWN_TYPE";

export type QualityIssueSummary = {
  type: QualityIssueType;
  message: string;
};

export type ColumnQuality = {
  columnName: string;
  columnPosition: number;
  qualityScore: number;
  nullPercentage: number;
  uniquenessPercentage: number;
  emptyPercentage: number;
  typeConsistencyScore: number;
  issueSummaries: QualityIssueSummary[];
};

export type DatasetQualityResponse = {
  dataset: Dataset;
  overallScore: number;
  issueSummaries: QualityIssueSummary[];
  scoredAt: string;
  columns: ColumnQuality[];
};

export type AiInsightGenerationStatus = "GENERATED" | "UNAVAILABLE";

export type DatasetAiInsightResponse = {
  dataset: Dataset;
  generationStatus: AiInsightGenerationStatus;
  modelName: string;
  datasetDescription: string;
  potentialIssues: string[];
  suggestedAnalyses: string[];
  suggestedVisualizations: string[];
  errorMessage: string | null;
  generatedAt: string;
};

export type CleaningRule =
  | "TRIM_WHITESPACE"
  | "NORMALIZE_BLANK_VALUES"
  | "NORMALIZE_COLUMN_NAMES_TO_SNAKE_CASE"
  | "REMOVE_FULLY_EMPTY_ROWS"
  | "REMOVE_DUPLICATE_ROWS";

export type ColumnRename = {
  originalName: string;
  cleanedName: string;
};

export type DatasetCleaningReportResponse = {
  dataset: Dataset;
  cleanedFilename: string;
  cleanedFileSizeBytes: number;
  rowsRead: number;
  rowsWritten: number;
  duplicateRowsRemoved: number;
  emptyRowsRemoved: number;
  columnsRenamed: ColumnRename[];
  cleaningRulesApplied: CleaningRule[];
  cleanedAt: string;
};

export type CleanedDatasetDownload = {
  blob: Blob;
  filename: string;
};

export type DatasetChatResponse = {
  answer: string;
};
