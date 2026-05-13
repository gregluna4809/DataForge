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
