import {
  createContext,
  type PropsWithChildren,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";
import { authStorage } from "@/context/authStorage";
import type { AuthResponse, UserSummary } from "@/types/auth";

type AuthContextValue = {
  accessToken: string | null;
  user: UserSummary | null;
  isAuthenticated: boolean;
  setAuthenticatedSession: (response: AuthResponse) => void;
  signOut: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: PropsWithChildren) {
  const [accessToken, setAccessToken] = useState<string | null>(() => authStorage.getToken());
  const [user, setUser] = useState<UserSummary | null>(() => authStorage.getUser());

  const setAuthenticatedSession = useCallback((response: AuthResponse) => {
    authStorage.setSession(response.accessToken, response.user);
    setAccessToken(response.accessToken);
    setUser(response.user);
  }, []);

  const signOut = useCallback(() => {
    authStorage.clear();
    setAccessToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      accessToken,
      user,
      isAuthenticated: Boolean(accessToken),
      setAuthenticatedSession,
      signOut,
    }),
    [accessToken, user, setAuthenticatedSession, signOut],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return context;
}
