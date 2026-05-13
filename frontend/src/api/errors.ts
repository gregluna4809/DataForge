import { AxiosError } from "axios";
import type { ApiErrorResponse } from "@/types/api";

export function getApiErrorMessages(error: unknown, fallbackMessage: string) {
  if (!(error instanceof AxiosError)) {
    return [error instanceof Error ? error.message : fallbackMessage];
  }

  const response = error.response?.data as ApiErrorResponse | undefined;
  if (!response?.message) {
    return [error.message || fallbackMessage];
  }

  return response.message
    .split(";")
    .map((message) => message.trim())
    .filter(Boolean);
}
