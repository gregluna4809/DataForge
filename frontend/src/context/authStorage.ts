import type { UserSummary } from "@/types/auth";

const TOKEN_KEY = "dataforge.accessToken";
const USER_KEY = "dataforge.user";

export const authStorage = {
  getToken() {
    return window.localStorage.getItem(TOKEN_KEY);
  },
  setSession(token: string, user: UserSummary) {
    window.localStorage.setItem(TOKEN_KEY, token);
    window.localStorage.setItem(USER_KEY, JSON.stringify(user));
  },
  getUser() {
    const rawUser = window.localStorage.getItem(USER_KEY);
    if (!rawUser) {
      return null;
    }

    try {
      return JSON.parse(rawUser) as UserSummary;
    } catch {
      window.localStorage.removeItem(USER_KEY);
      return null;
    }
  },
  clear() {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.removeItem(USER_KEY);
  },
};
