import { apiClient } from "@/api/client";
import type { AxiosProgressEvent } from "axios";
import type {
  CreateDatasetRequest,
  Dataset,
  DatasetAiInsightResponse,
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
