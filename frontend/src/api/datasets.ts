import { apiClient } from "@/api/client";
import type { AxiosProgressEvent } from "axios";
import type {
  CleanedDatasetDownload,
  CreateDatasetRequest,
  Dataset,
  DatasetAiInsightResponse,
  DatasetChatResponse,
  DatasetCleaningReportResponse,
  DatasetPreviewResponse,
  DatasetProfileResponse,
  DatasetQualityResponse,
  DatasetUploadResponse,
} from "@/types/datasets";

export async function getDatasets() {
  const response = await apiClient.get<Dataset[]>("/api/datasets");
  return response.data;
}

export async function createDataset(request: CreateDatasetRequest) {
  const response = await apiClient.post<Dataset>("/api/datasets", request);
  return response.data;
}

export async function uploadDatasetCsv(
  datasetId: string,
  file: File,
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void,
) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await apiClient.post<DatasetUploadResponse>(`/api/datasets/${datasetId}/upload`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
    onUploadProgress,
  });

  return response.data;
}

export async function getDatasetPreview(datasetId: string) {
  const response = await apiClient.get<DatasetPreviewResponse>(`/api/datasets/${datasetId}/preview`);
  return response.data;
}

export async function getDatasetProfile(datasetId: string) {
  const response = await apiClient.get<DatasetProfileResponse>(`/api/datasets/${datasetId}/profile`);
  return response.data;
}

export async function getDatasetQuality(datasetId: string) {
  const response = await apiClient.get<DatasetQualityResponse>(`/api/datasets/${datasetId}/quality`);
  return response.data;
}

export async function getDatasetInsights(datasetId: string) {
  const response = await apiClient.get<DatasetAiInsightResponse>(`/api/datasets/${datasetId}/insights`);
  return response.data;
}

export async function cleanDataset(datasetId: string) {
  const response = await apiClient.post<DatasetCleaningReportResponse>(`/api/datasets/${datasetId}/clean`);
  return response.data;
}

export async function getDatasetCleaningReport(datasetId: string) {
  const response = await apiClient.get<DatasetCleaningReportResponse>(`/api/datasets/${datasetId}/cleaning-report`);
  return response.data;
}

export async function downloadCleanedDataset(datasetId: string): Promise<CleanedDatasetDownload> {
  const response = await apiClient.get<Blob>(`/api/datasets/${datasetId}/download-cleaned`, {
    responseType: "blob",
  });

  return {
    blob: response.data,
    filename: getFilenameFromContentDisposition(response.headers["content-disposition"], "cleaned-dataset.csv"),
  };
}

export async function chatWithDataset(datasetId: string, message: string) {
  const response = await apiClient.post<DatasetChatResponse>(`/api/datasets/${datasetId}/chat`, { message });
  return response.data;
}

function getFilenameFromContentDisposition(value: unknown, fallback: string) {
  if (typeof value !== "string") {
    return fallback;
  }

  const filenameMatch = value.match(/filename="?([^"]+)"?/i);
  return filenameMatch?.[1] ?? fallback;
}
