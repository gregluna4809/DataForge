import { AxiosError } from "axios";
import type { ApiErrorResponse } from "@/types/api";

export function getApiErrorMessages(error: unknown, fallbackMessage: string) {
  if (!(error instanceof AxiosError)) {
    return [fallbackMessage];
  }

  const response = error.response?.data as ApiErrorResponse | undefined;
  if (!response?.message) {
    return [fallbackMessage];
  }

  return response.message
    .split(";")
    .map((message) => message.trim())
    .filter(Boolean);
}
