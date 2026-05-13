import { Link } from "react-router-dom";
import { useMemo } from "react";
import { useQueries, useQuery } from "@tanstack/react-query";
import { Activity, AlertTriangle, Database, FileCheck2, Loader2, ShieldCheck } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getApiErrorMessages } from "@/api/errors";
import { getDatasetProfile, getDatasetQuality, getDatasets } from "@/api/datasets";
import type { DatasetProfileResponse, DatasetQualityResponse } from "@/types/datasets";

const pipelineSteps = [
  { label: "Upload", href: "/upload", description: "Create metadata and upload a CSV file.", badge: "Available" },
  { label: "Preview", href: "/datasets", description: "Open a dataset to inspect parsed preview rows.", badge: "Available" },
  { label: "Profile", href: "/datasets", description: "Open a dataset to review column profile results.", badge: "Available" },
  { label: "Quality", href: "/datasets", description: "Open a dataset to review quality scoring.", badge: "Available" },
  { label: "Insights", href: "/datasets", description: "Open a dataset to review AI insight snapshots.", badge: "Available" },
];

export function DashboardPage() {
  const datasetsQuery = useQuery({
    queryKey: ["datasets"],
    queryFn: getDatasets,
  });

  const datasets = datasetsQuery.data ?? [];

  const qualityQueries = useQueries({
    queries: datasets.map((dataset) => ({
      queryKey: ["datasets", dataset.id, "quality"],
      queryFn: () => getDatasetQuality(dataset.id),
      enabled: datasetsQuery.isSuccess,
      retry: 1,
    })),
  });

  const profileQueries = useQueries({
    queries: datasets.map((dataset) => ({
      queryKey: ["datasets", dataset.id, "profile"],
      queryFn: () => getDatasetProfile(dataset.id),
      enabled: datasetsQuery.isSuccess,
      retry: 1,
    })),
  });

  const dashboardMetrics = useMemo(() => {
    const successfulQuality = qualityQueries
      .map((query) => query.data as DatasetQualityResponse | undefined)
      .filter((quality): quality is DatasetQualityResponse => Boolean(quality));
    const successfulProfiles = profileQueries
      .map((query) => query.data as DatasetProfileResponse | undefined)
      .filter((profile): profile is DatasetProfileResponse => Boolean(profile));

    const totalIssues = successfulQuality.reduce((sum, quality) => sum + quality.issueSummaries.length, 0);
    const totalProfileColumns = successfulProfiles.reduce((sum, profile) => sum + profile.columns.length, 0);
    const averageQuality =
      successfulQuality.length > 0
        ? successfulQuality.reduce((sum, quality) => sum + quality.overallScore, 0) / successfulQuality.length
        : null;

    return {
      totalDatasets: datasets.length,
      averageQuality,
      totalProfileColumns,
      totalIssues,
      analyzedDatasets: successfulQuality.length,
      partialFailures: qualityQueries.filter((query) => query.isError).length + profileQueries.filter((query) => query.isError).length,
    };
  }, [datasets.length, profileQueries, qualityQueries]);

  const metrics = [
    {
      label: "Datasets tracked",
      value: formatNumber(dashboardMetrics.totalDatasets),
      detail: "Owned dataset records",
      icon: Database,
    },
    {
      label: "Average quality",
      value: dashboardMetrics.averageQuality == null ? "Pending" : dashboardMetrics.averageQuality.toFixed(2),
      detail: `${dashboardMetrics.analyzedDatasets} datasets scored`,
      icon: ShieldCheck,
    },
    {
      label: "Profile columns",
      value: formatNumber(dashboardMetrics.totalProfileColumns),
      detail: "Columns analyzed",
      icon: FileCheck2,
    },
    {
      label: "Quality issues",
      value: formatNumber(dashboardMetrics.totalIssues),
      detail: "Dataset-level issue summaries",
      icon: AlertTriangle,
    },
  ];

  return (
    <div className="space-y-6">
      <section className="flex flex-col justify-between gap-4 rounded-lg border bg-card p-6 shadow-panel lg:flex-row lg:items-center">
        <div>
          <Badge variant="default">Backend-driven metrics</Badge>
          <h2 className="mt-4 text-2xl font-semibold tracking-normal">Operational data quality workspace</h2>
          <p className="mt-2 max-w-3xl text-sm leading-6 text-muted-foreground">
            Monitor dataset ingestion, preview parsing, deterministic profiling, quality scoring, and AI insight readiness from existing backend analysis endpoints.
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

      {datasetsQuery.isError ? (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          <p className="font-medium">Dashboard metrics unavailable</p>
          <p className="mt-1">{getApiErrorMessages(datasetsQuery.error, "Datasets could not be loaded.").join(" ")}</p>
        </div>
      ) : null}

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {metrics.map((metric) => (
          <Card key={metric.label}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-3">
              <CardDescription>{metric.label}</CardDescription>
              {datasetsQuery.isLoading ? <Loader2 className="h-4 w-4 animate-spin text-primary" /> : <metric.icon className="h-4 w-4 text-muted-foreground" />}
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-semibold">{datasetsQuery.isLoading ? "Loading" : metric.value}</p>
              <p className="mt-1 text-sm text-muted-foreground">{metric.detail}</p>
            </CardContent>
          </Card>
        ))}
      </section>

      <section className="grid gap-4 xl:grid-cols-[1.4fr_1fr]">
        <Card>
          <CardHeader>
            <CardTitle>Analytics pipeline status</CardTitle>
            <CardDescription>Navigate into the workflows backed by current DataForge APIs.</CardDescription>
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
            <CardTitle>Analysis coverage</CardTitle>
            <CardDescription>Rollup status from profile and quality endpoint requests.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <CoverageItem label="Datasets loaded" value={formatNumber(dashboardMetrics.totalDatasets)} />
            <CoverageItem label="Quality responses" value={formatNumber(dashboardMetrics.analyzedDatasets)} />
            <CoverageItem label="Profile responses" value={formatNumber(profileQueries.filter((query) => query.data).length)} />
            <CoverageItem label="Unavailable analyses" value={formatNumber(dashboardMetrics.partialFailures)} />
          </CardContent>
        </Card>
      </section>
    </div>
  );
}

function CoverageItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex gap-3">
      <div className="mt-1 flex h-7 w-7 items-center justify-center rounded-md bg-secondary">
        <Activity className="h-4 w-4 text-primary" />
      </div>
      <div>
        <p className="text-sm font-medium">{label}</p>
        <p className="text-xs text-muted-foreground">{value}</p>
      </div>
    </div>
  );
}

function formatNumber(value: number) {
  return new Intl.NumberFormat().format(value);
}
