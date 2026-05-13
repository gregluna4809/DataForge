import { KeyRound, Server, UserCog } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export function SettingsPage() {
  return (
    <div className="space-y-6">
      <section>
        <h2 className="text-2xl font-semibold tracking-normal">Settings</h2>
        <p className="mt-1 text-sm text-muted-foreground">Review workspace defaults and integration assumptions for local development.</p>
      </section>

      <div className="grid gap-4 xl:grid-cols-3">
        <Card>
          <CardHeader>
            <div className="flex items-center gap-3">
              <UserCog className="h-5 w-5 text-primary" />
              <CardTitle>Workspace</CardTitle>
            </div>
            <CardDescription>Local shell settings for account-level UI.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="workspace-name">Workspace name</Label>
              <Input id="workspace-name" value="DataForge Analytics" readOnly />
            </div>
            <Badge variant="secondary">Frontend shell</Badge>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center gap-3">
              <Server className="h-5 w-5 text-primary" />
              <CardTitle>API</CardTitle>
            </div>
            <CardDescription>Axios uses this base URL for backend requests.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <Label htmlFor="api-url">VITE_API_BASE_URL</Label>
            <Input id="api-url" value={import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080"} readOnly />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center gap-3">
              <KeyRound className="h-5 w-5 text-primary" />
              <CardTitle>Session</CardTitle>
            </div>
            <CardDescription>JWT access tokens are persisted locally for protected routing.</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm leading-6 text-muted-foreground">
              The frontend stores the backend access token and user summary in local storage. Future hardening can add token refresh and session expiry handling.
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
