export type UserSummary = {
  id: string;
  email: string;
  name: string;
  createdAt: string;
};

export type AuthResponse = {
  tokenType: "Bearer";
  accessToken: string;
  user: UserSummary;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
};
