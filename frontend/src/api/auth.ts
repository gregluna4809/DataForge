import { apiClient } from "@/api/client";
import type { AuthResponse, LoginRequest, RegisterRequest } from "@/types/auth";

export async function login(request: LoginRequest) {
  const response = await apiClient.post<AuthResponse>("/api/auth/login", request);
  return response.data;
}

export async function register(request: RegisterRequest) {
  const response = await apiClient.post<AuthResponse>("/api/auth/register", request);
  return response.data;
}
