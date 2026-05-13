import { apiClient } from "@/api/client";
import type { AuthResponse, LoginRequest, RegisterRequest } from "@/types/auth";

export async function login(request: LoginRequest) {
  const response = await apiClient.post<AuthResponse>("/api/auth/login", request);
  assertAuthResponse(response.data);
  return response.data;
}

export async function register(request: RegisterRequest) {
  const response = await apiClient.post<AuthResponse>("/api/auth/register", request);
  assertAuthResponse(response.data);
  return response.data;
}

function assertAuthResponse(response: AuthResponse) {
  if (!response.accessToken || !response.user) {
    throw new Error("Authentication response did not include an access token and user.");
  }
}
