import { Link } from "react-router-dom";
import { Activity, AlertTriangle, Database, FileCheck2, ShieldCheck } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

const metrics = [
  { label: "Datasets tracked", value: "12", detail: "4 ready for review", icon: Database },
  { label: "Average quality", value: "91.4", detail: "Preview-based score", icon: ShieldCheck },
  { label: "Profiles generated", value: "38", detail: "Column-level summaries", icon: FileCheck2 },
  { label: "Open issues", value: "7", detail: "Nulls and type warnings", icon: AlertTriangle },
];

const activityItems = [
  "Customer churn sample uploaded",
  "Quality scoring completed for revenue_export.csv",
  "AI insight snapshot cached for operations_metrics.csv",
  "Dataset ownership validation passed",
];

const pipelineSteps = [
  { label: "Upload", href: "/upload", description: "Create metadata and upload a CSV file.", badge: "Available" },
  { label: "Preview", href: "/datasets", description: "Open a dataset to inspect parsed preview rows.", badge: "Available" },
  { label: "Profile", href: "/datasets", description: "Open a dataset to review column profile results.", badge: "Available" },
  { label: "Quality", href: "/datasets", description: "Open a dataset to review quality scoring.", badge: "Available" },
  { label: "Insights", href: "/datasets", description: "Open a dataset to review AI insight snapshots.", badge: "Available" },
];

export function DashboardPage() {
  return (
    <div className="space-y-6">
      <section className="flex flex-col justify-between gap-4 rounded-lg border bg-card p-6 shadow-panel lg:flex-row lg:items-center">
        <div>
          <Badge variant="default">Backend foundation connected</Badge>
          <h2 className="mt-4 text-2xl font-semibold tracking-normal">Operational data quality workspace</h2>
          <p className="mt-2 max-w-3xl text-sm leading-6 text-muted-foreground">
            Monitor dataset ingestion, preview parsing, deterministic profiling, quality scoring, and AI insight readiness from a single dashboard shell.
          </p>
        </div>
        <div className="grid min-w-72 grid-cols-2 gap-3 text-sm">
          <div className="rounded-lg border bg-background p-4">
            <p className="font-semibold">JWT Auth</p>
            <p className="mt-1 text-muted-foreground">Session protected</p>
          </div>
          <div className="rounded-lg border bg-background p-4">
            <p className="font-semibold">PostgreSQL</p>
            <p className="mt-1 text-muted-foreground">Flyway managed</p>
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {metrics.map((metric) => (
          <Card key={metric.label}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-3">
              <CardDescription>{metric.label}</CardDescription>
              <metric.icon className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-semibold">{metric.value}</p>
              <p className="mt-1 text-sm text-muted-foreground">{metric.detail}</p>
            </CardContent>
          </Card>
        ))}
      </section>

      <section className="grid gap-4 xl:grid-cols-[1.4fr_1fr]">
        <Card>
          <CardHeader>
            <CardTitle>Analytics pipeline status</CardTitle>
            <CardDescription>Frontend shell prepared for progressive endpoint integration.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {pipelineSteps.map((step, index) => (
              <Link
                key={step.label}
                to={step.href}
                className="flex cursor-pointer items-center gap-4 rounded-lg border bg-background p-3 transition-colors hover:border-primary/40 hover:bg-primary/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <div className="flex h-8 w-8 items-center justify-center rounded-md bg-primary/10 text-sm font-semibold text-primary">
                  {index + 1}
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium">{step.label}</p>
                  <p className="text-xs text-muted-foreground">{step.description}</p>
                </div>
                <Badge variant="default">{step.badge}</Badge>
              </Link>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Recent activity</CardTitle>
            <CardDescription>Representative workspace events for layout validation.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {activityItems.map((item) => (
              <div key={item} className="flex gap-3">
                <div className="mt-1 flex h-7 w-7 items-center justify-center rounded-md bg-secondary">
                  <Activity className="h-4 w-4 text-primary" />
                </div>
                <div>
                  <p className="text-sm font-medium">{item}</p>
                  <p className="text-xs text-muted-foreground">Workspace event</p>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
