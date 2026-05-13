import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";

type RedirectState = {
  from?: {
    pathname?: string;
  };
};

export function PublicOnlyRoute() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const redirectTarget = (location.state as RedirectState | null)?.from?.pathname ?? "/dashboard";

  if (isAuthenticated) {
    return <Navigate to={redirectTarget} replace />;
  }

  return <Outlet />;
}
