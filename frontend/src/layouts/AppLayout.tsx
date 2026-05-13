import { useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  BarChart3,
  Database,
  FileUp,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  Settings,
  Sparkles,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";

const navigation = [
  { label: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
  { label: "Datasets", href: "/datasets", icon: Database },
  { label: "Upload", href: "/upload", icon: FileUp },
  { label: "Insights", href: "/insights", icon: Sparkles },
  { label: "Settings", href: "/settings", icon: Settings },
];

const pageTitles: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/datasets": "Datasets",
  "/upload": "Upload",
  "/insights": "Insights",
  "/settings": "Settings",
};

export function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const title = pageTitles[location.pathname] ?? (location.pathname.startsWith("/datasets/") ? "Dataset Detail" : "DataForge");

  function handleSignOut() {
    signOut();
    navigate("/login", { replace: true });
  }

  return (
    <div className="min-h-screen bg-background">
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-40 w-72 border-r bg-card transition-transform lg:translate-x-0",
          sidebarOpen ? "translate-x-0" : "-translate-x-full",
        )}
      >
        <div className="flex h-full flex-col">
          <div className="flex h-16 items-center justify-between px-5">
            <div className="flex items-center gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-sm font-semibold text-primary-foreground">
                DF
              </div>
              <div>
                <p className="text-sm font-semibold">DataForge</p>
                <p className="text-xs text-muted-foreground">Governance Console</p>
              </div>
            </div>
            <Button className="lg:hidden" variant="ghost" size="icon" onClick={() => setSidebarOpen(false)}>
              <X />
            </Button>
          </div>
          <Separator />
          <nav className="flex-1 space-y-1 px-3 py-4">
            {navigation.map((item) => (
              <NavLink
                key={item.href}
                to={item.href}
                onClick={() => setSidebarOpen(false)}
                className={({ isActive }) =>
                  cn(
                    "flex h-10 items-center gap-3 rounded-md px-3 text-sm font-medium text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground",
                    isActive && "bg-secondary text-foreground",
                  )
                }
              >
                <item.icon className="h-4 w-4" />
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="border-t p-4">
            <div className="rounded-lg border bg-background p-3">
              <div className="flex items-center gap-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-md bg-secondary text-xs font-semibold">
                  {user?.name?.slice(0, 2).toUpperCase() ?? "DF"}
                </div>
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium">{user?.name ?? "DataForge User"}</p>
                  <p className="truncate text-xs text-muted-foreground">{user?.email ?? "Authenticated session"}</p>
                </div>
              </div>
              <Button className="mt-3 w-full justify-start" variant="ghost" size="sm" onClick={handleSignOut}>
                <LogOut />
                Sign out
              </Button>
            </div>
          </div>
        </div>
      </aside>

      {sidebarOpen ? (
        <div className="fixed inset-0 z-30 bg-foreground/20 lg:hidden" onClick={() => setSidebarOpen(false)} />
      ) : null}

      <div className="lg:pl-72">
        <header className="sticky top-0 z-20 border-b bg-card/95 backdrop-blur">
          <div className="flex h-16 items-center gap-4 px-4 sm:px-6 lg:px-8">
            <Button className="lg:hidden" variant="ghost" size="icon" onClick={() => setSidebarOpen(true)}>
              <Menu />
            </Button>
            <div className="min-w-0 flex-1">
              <p className="text-xs font-medium uppercase tracking-[0.18em] text-muted-foreground">Workspace</p>
              <h1 className="truncate text-lg font-semibold">{title}</h1>
            </div>
            <div className="hidden h-10 w-72 items-center gap-2 rounded-md border bg-background px-3 text-sm text-muted-foreground md:flex">
              <Search className="h-4 w-4" />
              Search datasets
            </div>
            <Button>
              <BarChart3 />
              New analysis
            </Button>
          </div>
        </header>
        <main className="px-4 py-6 sm:px-6 lg:px-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
