import { apiClient } from "@/api/client";
import type { CreateDatasetRequest, Dataset } from "@/types/datasets";

export async function getDatasets() {
  const response = await apiClient.get<Dataset[]>("/api/datasets");
  return response.data;
}

export async function createDataset(request: CreateDatasetRequest) {
  const response = await apiClient.post<Dataset>("/api/datasets", request);
  return response.data;
}
