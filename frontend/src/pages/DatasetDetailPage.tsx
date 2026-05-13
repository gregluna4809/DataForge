import { Link, useParams } from "react-router-dom";
import { type ReactNode, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AxiosError } from "axios";
import {
  AlertCircle,
  Brain,
  CheckCircle2,
  Database,
  Download,
  FileText,
  Loader2,
  ShieldCheck,
  TableProperties,
} from "lucide-react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  cleanDataset,
  downloadCleanedDataset,
  getDatasetCleaningReport,
  getDatasetInsights,
  getDatasetPreview,
  getDatasetProfile,
  getDatasetQuality,
} from "@/api/datasets";
import { getApiErrorMessages } from "@/api/errors";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import type {
  ColumnProfile,
  Dataset,
  DatasetAiInsightResponse,
  DatasetCleaningReportResponse,
  DatasetPreviewResponse,
  DatasetProfileResponse,
  DatasetQualityResponse,
  DatasetStatus,
  QualityIssueSummary,
} from "@/types/datasets";

const CHART_COLORS = ["#28786f", "#f2a51a", "#475569", "#0f766e", "#dc2626", "#64748b", "#7c3aed"];

export function DatasetDetailPage() {
  const { datasetId } = useParams();
  const queryClient = useQueryClient();
  const [selectedColumnPosition, setSelectedColumnPosition] = useState<number | null>(null);
  const enabled = Boolean(datasetId);

  const previewQuery = useQuery({
    queryKey: ["datasets", datasetId, "preview"],
    queryFn: () => getDatasetPreview(datasetId!),
    enabled,
  });

  const profileQuery = useQuery({
    queryKey: ["datasets", datasetId, "profile"],
    queryFn: () => getDatasetProfile(datasetId!),
    enabled,
  });

  const qualityQuery = useQuery({
    queryKey: ["datasets", datasetId, "quality"],
    queryFn: () => getDatasetQuality(datasetId!),
    enabled,
  });

  const insightsQuery = useQuery({
    queryKey: ["datasets", datasetId, "insights"],
    queryFn: () => getDatasetInsights(datasetId!),
    enabled,
  });

  const cleaningReportQuery = useQuery({
    queryKey: ["datasets", datasetId, "cleaning-report"],
    queryFn: () => getDatasetCleaningReport(datasetId!),
    enabled,
    retry: false,
  });

  const cleanMutation = useMutation({
    mutationFn: () => cleanDataset(datasetId!),
    onSuccess: async (report) => {
      queryClient.setQueryData(["datasets", datasetId, "cleaning-report"], report);
      await queryClient.invalidateQueries({ queryKey: ["datasets"] });
    },
  });

  const downloadMutation = useMutation({
    mutationFn: () => downloadCleanedDataset(datasetId!),
    onSuccess: ({ blob, filename }) => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    },
  });

  const dataset =
    previewQuery.data?.dataset ??
    profileQuery.data?.dataset ??
    qualityQuery.data?.dataset ??
    insightsQuery.data?.dataset ??
    cleaningReportQuery.data?.dataset;
  const selectedColumn = useMemo(() => {
    const columns = profileQuery.data?.columns ?? [];
    if (columns.length === 0) {
      return null;
    }

    return columns.find((column) => column.columnPosition === selectedColumnPosition) ?? columns[0];
  }, [profileQuery.data?.columns, selectedColumnPosition]);

  if (!datasetId) {
    return (
      <SectionError title="Dataset not found" messages={["The route did not include a dataset identifier."]} />
    );
  }

  return (
    <div className="space-y-6">
      <MetadataSummary
        dataset={dataset}
        quality={qualityQuery.data}
        loading={!dataset && (previewQuery.isLoading || profileQuery.isLoading || qualityQuery.isLoading || insightsQuery.isLoading)}
      />

      <CleaningSection
        report={cleaningReportQuery.data}
        reportLoading={cleaningReportQuery.isLoading}
        reportError={cleaningReportQuery.error}
        cleanPending={cleanMutation.isPending}
        cleanSuccess={cleanMutation.isSuccess}
        cleanError={cleanMutation.error}
        downloadPending={downloadMutation.isPending}
        downloadError={downloadMutation.error}
        onClean={() => cleanMutation.mutate()}
        onDownload={() => downloadMutation.mutate()}
      />

      <section className="grid gap-6 xl:grid-cols-[1.35fr_0.9fr]">
        <PreviewSection query={previewQuery} />
        <QualitySection query={qualityQuery} />
      </section>

      <ProfileSection query={profileQuery} />

      <VisualAnalyticsSection
        profile={profileQuery.data}
        quality={qualityQuery.data}
        selectedColumn={selectedColumn}
        selectedColumnPosition={selectedColumn?.columnPosition ?? null}
        onSelectColumn={setSelectedColumnPosition}
      />

      <InsightsSection query={insightsQuery} />
    </div>
  );
}

function VisualAnalyticsSection({
  profile,
  quality,
  selectedColumn,
  selectedColumnPosition,
  onSelectColumn,
}: {
  profile: DatasetProfileResponse | undefined;
  quality: DatasetQualityResponse | undefined;
  selectedColumn: ColumnProfile | null;
  selectedColumnPosition: number | null;
  onSelectColumn: (position: number) => void;
}) {
  const issueDistribution = useMemo(() => {
    const issueCounts = new Map<string, number>();
    for (const issue of quality?.issueSummaries ?? []) {
      issueCounts.set(issue.type, (issueCounts.get(issue.type) ?? 0) + 1);
    }

    return Array.from(issueCounts.entries()).map(([name, value]) => ({
      name: name.replace(/_/g, " "),
      value,
    }));
  }, [quality?.issueSummaries]);

  const typeBreakdown = useMemo(() => {
    const typeCounts = new Map<string, number>();
    for (const column of profile?.columns ?? []) {
      typeCounts.set(column.inferredDataType, (typeCounts.get(column.inferredDataType) ?? 0) + 1);
    }

    return Array.from(typeCounts.entries()).map(([name, value]) => ({ name, value }));
  }, [profile?.columns]);

  const nullComparison = useMemo(
    () =>
      (profile?.columns ?? []).map((column) => ({
        name: column.columnName,
        nullCount: column.nullCount,
        nonNullCount: column.nonNullCount,
      })),
    [profile?.columns],
  );

  const topValues = useMemo(
    () =>
      (selectedColumn?.mostCommonValues ?? []).map((item) => ({
        value: item.value || "Empty",
        count: item.count,
      })),
    [selectedColumn?.mostCommonValues],
  );

  return (
    <Card>
      <CardHeader>
        <CardTitle>Visual analytics</CardTitle>
        <CardDescription>Charts generated from profile and quality endpoint responses.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {!profile && !quality ? (
          <EmptyPanel title="Charts unavailable" description="Profile or quality results are required before charts can be rendered." />
        ) : null}

        <div className="grid gap-4 xl:grid-cols-2">
          <ChartPanel title="Quality issue distribution" description="Dataset-level issue summary counts.">
            {issueDistribution.length > 0 ? (
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={issueDistribution} dataKey="value" nameKey="name" outerRadius={88} innerRadius={48} paddingAngle={3}>
                    {issueDistribution.map((entry, index) => (
                      <Cell key={entry.name} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <ChartEmpty message="No quality issues returned." />
            )}
          </ChartPanel>

          <ChartPanel title="Inferred data types" description="Column count by inferred backend data type.">
            {typeBreakdown.length > 0 ? (
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={typeBreakdown}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="name" tickLine={false} axisLine={false} fontSize={12} />
                  <YAxis allowDecimals={false} tickLine={false} axisLine={false} fontSize={12} />
                  <Tooltip />
                  <Bar dataKey="value" name="Columns" radius={[6, 6, 0, 0]} fill="#28786f" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <ChartEmpty message="No column profile types returned." />
            )}
          </ChartPanel>
        </div>

        <div className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
          <ChartPanel title="Null vs non-null by column" description="Preview-value completeness by profiled column.">
            {nullComparison.length > 0 ? (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart data={nullComparison} margin={{ left: 8, right: 8 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="name" tickLine={false} axisLine={false} fontSize={12} interval={0} angle={-20} textAnchor="end" height={70} />
                  <YAxis allowDecimals={false} tickLine={false} axisLine={false} fontSize={12} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="nonNullCount" name="Non-null" stackId="values" fill="#28786f" radius={[0, 0, 0, 0]} />
                  <Bar dataKey="nullCount" name="Null" stackId="values" fill="#f2a51a" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <ChartEmpty message="No null/non-null profile data returned." />
            )}
          </ChartPanel>

          <ChartPanel title="Top values" description="Most common values for the selected column.">
            {profile?.columns && profile.columns.length > 0 ? (
              <div className="mb-4">
                <label className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground" htmlFor="top-values-column">
                  Column
                </label>
                <select
                  id="top-values-column"
                  className="mt-2 h-10 w-full rounded-md border border-input bg-card px-3 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  value={selectedColumnPosition ?? profile.columns[0].columnPosition}
                  onChange={(event) => onSelectColumn(Number(event.target.value))}
                >
                  {profile.columns.map((column) => (
                    <option key={`${column.columnName}-${column.columnPosition}`} value={column.columnPosition}>
                      {column.columnName}
                    </option>
                  ))}
                </select>
              </div>
            ) : null}

            {topValues.length > 0 ? (
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={topValues} layout="vertical" margin={{ left: 24, right: 8 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                  <XAxis type="number" allowDecimals={false} tickLine={false} axisLine={false} fontSize={12} />
                  <YAxis dataKey="value" type="category" tickLine={false} axisLine={false} fontSize={12} width={90} />
                  <Tooltip />
                  <Bar dataKey="count" name="Count" radius={[0, 6, 6, 0]} fill="#28786f" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <ChartEmpty message="No common values returned for this column." />
            )}
          </ChartPanel>
        </div>
      </CardContent>
    </Card>
  );
}

function MetadataSummary({
  dataset,
  quality,
  loading,
}: {
  dataset: Dataset | undefined;
  quality: DatasetQualityResponse | undefined;
  loading: boolean;
}) {
  if (loading) {
    return <LoadingPanel title="Loading dataset" description="Fetching metadata and analysis snapshots." />;
  }

  if (!dataset) {
    return (
      <section className="rounded-lg border bg-card p-6 shadow-panel">
        <Badge variant="secondary">Dataset detail</Badge>
        <h2 className="mt-4 text-2xl font-semibold tracking-normal">Analysis unavailable</h2>
        <p className="mt-2 text-sm text-muted-foreground">No dataset metadata has been returned yet.</p>
      </section>
    );
  }

  return (
    <section className="rounded-lg border bg-card p-6 shadow-panel">
      <div className="flex flex-col justify-between gap-5 lg:flex-row lg:items-start">
        <div>
          <StatusBadge status={dataset.status} />
          <h2 className="mt-4 text-2xl font-semibold tracking-normal">{dataset.name}</h2>
          <p className="mt-2 max-w-3xl text-sm leading-6 text-muted-foreground">
            {dataset.description ?? "Dataset analysis generated from uploaded CSV preview, profile, quality, and AI insight endpoints."}
          </p>
          <div className="mt-4 flex flex-wrap gap-2 text-sm text-muted-foreground">
            <span>{dataset.originalFilename}</span>
            <span>/</span>
            <span>Owner {dataset.uploadedBy.email}</span>
          </div>
        </div>
        <div className="grid gap-3 text-sm sm:grid-cols-2 lg:grid-cols-4">
          <SummaryMetric label="Rows" value={formatNumber(dataset.rowCount)} />
          <SummaryMetric label="Columns" value={formatNumber(dataset.columnCount)} />
          <SummaryMetric label="File size" value={formatFileSize(dataset.fileSizeBytes)} />
          <SummaryMetric label="Quality" value={quality ? quality.overallScore.toFixed(2) : "Pending"} />
        </div>
      </div>
    </section>
  );
}

function CleaningSection({
  report,
  reportLoading,
  reportError,
  cleanPending,
  cleanSuccess,
  cleanError,
  downloadPending,
  downloadError,
  onClean,
  onDownload,
}: {
  report: DatasetCleaningReportResponse | undefined;
  reportLoading: boolean;
  reportError: Error | null;
  cleanPending: boolean;
  cleanSuccess: boolean;
  cleanError: Error | null;
  downloadPending: boolean;
  downloadError: Error | null;
  onClean: () => void;
  onDownload: () => void;
}) {
  const reportMissing = reportError ? isNotFoundError(reportError) : false;
  const hasCleanedFile = Boolean(report);

  return (
    <Card>
      <CardHeader>
        <div className="flex flex-col justify-between gap-4 lg:flex-row lg:items-start">
          <div className="flex items-start gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <div>
              <CardTitle>Deterministic cleaning</CardTitle>
              <CardDescription>
                Generate a cleaned CSV copy with whitespace trimming, normalized headers, empty-row removal, and duplicate-row removal.
              </CardDescription>
            </div>
          </div>
          <div className="flex flex-col gap-2 sm:flex-row lg:justify-end">
            <Button onClick={onClean} disabled={cleanPending} className="gap-2">
              {cleanPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
              {cleanPending ? "Cleaning" : "Clean Dataset"}
            </Button>
            <Button onClick={onDownload} disabled={!hasCleanedFile || downloadPending || cleanPending} variant="outline" className="gap-2">
              {downloadPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
              Download Cleaned CSV
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {cleanPending ? (
          <div className="rounded-lg border bg-background p-4">
            <div className="flex items-center justify-between gap-3 text-sm">
              <span className="font-medium">Cleaning job in progress</span>
              <span className="text-muted-foreground">Creating cleaned CSV copy</span>
            </div>
            <div className="mt-4 h-2 overflow-hidden rounded-full bg-muted">
              <div className="h-full w-2/3 animate-pulse rounded-full bg-primary" />
            </div>
          </div>
        ) : null}

        {cleanSuccess ? (
          <FeedbackPanel
            tone="success"
            title="Cleaned dataset is ready"
            messages={["The cleaning report has been refreshed and the cleaned CSV is available for download."]}
          />
        ) : null}

        {cleanError ? (
          <FeedbackPanel
            tone="error"
            title="Cleaning failed"
            messages={getApiErrorMessages(cleanError, "The backend could not clean this dataset.")}
          />
        ) : null}

        {downloadError ? (
          <FeedbackPanel
            tone="error"
            title="Download failed"
            messages={getApiErrorMessages(downloadError, "The cleaned CSV could not be downloaded.")}
          />
        ) : null}

        {reportLoading ? <LoadingInline label="Checking for existing cleaning report" /> : null}

        {reportError && !reportMissing ? (
          <FeedbackPanel
            tone="error"
            title="Cleaning report unavailable"
            messages={getApiErrorMessages(reportError, "The cleaning report could not be loaded.")}
          />
        ) : null}

        {!reportLoading && !report && (reportMissing || !reportError) ? (
          <EmptyPanel
            title="No cleaned file yet"
            description="Run deterministic cleaning to generate a report and enable cleaned CSV download."
          />
        ) : null}

        {report ? <CleaningReport report={report} /> : null}
      </CardContent>
    </Card>
  );
}

function CleaningReport({ report }: { report: DatasetCleaningReportResponse }) {
  return (
    <div className="space-y-4">
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-5">
        <SummaryMetric label="Rows read" value={formatNumber(report.rowsRead)} />
        <SummaryMetric label="Rows written" value={formatNumber(report.rowsWritten)} />
        <SummaryMetric label="Duplicates removed" value={formatNumber(report.duplicateRowsRemoved)} />
        <SummaryMetric label="Empty rows removed" value={formatNumber(report.emptyRowsRemoved)} />
        <SummaryMetric label="Cleaned size" value={formatFileSize(report.cleanedFileSizeBytes)} />
      </div>

      <div className="grid gap-4 xl:grid-cols-[0.95fr_1.05fr]">
        <div className="rounded-lg border bg-background p-4">
          <div className="flex items-center justify-between gap-3">
            <p className="font-medium">Columns renamed</p>
            <Badge variant="secondary">{formatNumber(report.columnsRenamed.length)}</Badge>
          </div>
          {report.columnsRenamed.length > 0 ? (
            <div className="mt-3 max-h-64 space-y-2 overflow-auto pr-1">
              {report.columnsRenamed.map((column) => (
                <div key={`${column.originalName}-${column.cleanedName}`} className="rounded-md border bg-card p-3 text-sm">
                  <p className="truncate text-muted-foreground">{column.originalName || "Blank column"}</p>
                  <p className="mt-1 truncate font-medium">{column.cleanedName}</p>
                </div>
              ))}
            </div>
          ) : (
            <p className="mt-3 text-sm text-muted-foreground">No column names required normalization.</p>
          )}
        </div>

        <div className="rounded-lg border bg-background p-4">
          <div className="flex items-center justify-between gap-3">
            <p className="font-medium">Cleaning rules applied</p>
            <Badge variant="outline">Deterministic</Badge>
          </div>
          <div className="mt-3 grid gap-2 sm:grid-cols-2">
            {report.cleaningRulesApplied.map((rule) => (
              <div key={rule} className="rounded-md border bg-card p-3 text-sm">
                <p className="font-medium">{formatEnumLabel(rule)}</p>
              </div>
            ))}
          </div>
          <p className="mt-4 text-xs text-muted-foreground">
            Cleaned {formatDate(report.cleanedAt)} / {report.cleanedFilename}
          </p>
        </div>
      </div>
    </div>
  );
}

function PreviewSection({ query }: { query: QueryState<DatasetPreviewResponse> }) {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
            <TableProperties className="h-5 w-5" />
          </div>
          <div>
            <CardTitle>Preview rows</CardTitle>
            <CardDescription>First parsed CSV rows returned by `GET /preview`.</CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {query.isLoading ? <LoadingInline label="Loading preview" /> : null}
        {query.isError ? <SectionError title="Preview unavailable" messages={getApiErrorMessages(query.error, "Preview rows could not be loaded.")} /> : null}
        {query.data ? (
          query.data.rows.length > 0 ? (
            <PreviewTable preview={query.data} />
          ) : (
            <EmptyPanel title="No preview rows" description="The CSV header was parsed, but no preview data rows were returned." />
          )
        ) : null}
      </CardContent>
    </Card>
  );
}

function PreviewTable({ preview }: { preview: DatasetPreviewResponse }) {
  return (
    <div className="overflow-auto rounded-lg border">
      <table className="w-full min-w-[760px] border-collapse text-left text-sm">
        <thead className="bg-muted text-xs uppercase tracking-[0.14em] text-muted-foreground">
          <tr>
            {preview.columnNames.map((columnName, index) => (
              <th key={`${columnName}-${index}`} className="border-b px-4 py-3 font-medium">
                {columnName || `Column ${index + 1}`}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {preview.rows.map((row, rowIndex) => (
            <tr key={rowIndex} className="border-b last:border-b-0">
              {preview.columnNames.map((_, columnIndex) => (
                <td key={`${rowIndex}-${columnIndex}`} className="max-w-64 truncate px-4 py-3 text-muted-foreground">
                  {row[columnIndex] || <span className="text-muted-foreground/60">Empty</span>}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function QualitySection({ query }: { query: QueryState<DatasetQualityResponse> }) {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
            <ShieldCheck className="h-5 w-5" />
          </div>
          <div>
            <CardTitle>Quality score</CardTitle>
            <CardDescription>Dataset and column quality summaries.</CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {query.isLoading ? <LoadingInline label="Loading quality score" /> : null}
        {query.isError ? <SectionError title="Quality unavailable" messages={getApiErrorMessages(query.error, "Quality results could not be loaded.")} /> : null}
        {query.data ? (
          <div className="space-y-4">
            <div className="rounded-lg border bg-background p-5">
              <p className="text-sm text-muted-foreground">Overall quality</p>
              <div className="mt-2 flex items-end gap-2">
                <p className="text-4xl font-semibold">{query.data.overallScore.toFixed(2)}</p>
                <p className="pb-1 text-sm text-muted-foreground">/ 100</p>
              </div>
              <p className="mt-2 text-xs text-muted-foreground">Scored {formatDate(query.data.scoredAt)}</p>
            </div>
            <IssueList issues={query.data.issueSummaries} emptyText="No dataset-level quality issues were returned." />
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}

function ProfileSection({ query }: { query: QueryState<DatasetProfileResponse> }) {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
            <Database className="h-5 w-5" />
          </div>
          <div>
            <CardTitle>Column profiles</CardTitle>
            <CardDescription>Per-column type inference, null counts, uniqueness, and common values.</CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {query.isLoading ? <LoadingInline label="Loading profile results" /> : null}
        {query.isError ? <SectionError title="Profile unavailable" messages={getApiErrorMessages(query.error, "Profile results could not be loaded.")} /> : null}
        {query.data ? (
          query.data.columns.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {query.data.columns.map((column) => (
                <ColumnProfileCard key={`${column.columnName}-${column.columnPosition}`} column={column} />
              ))}
            </div>
          ) : (
            <EmptyPanel title="No column profiles" description="The backend did not return profile records for this dataset." />
          )
        ) : null}
      </CardContent>
    </Card>
  );
}

function InsightsSection({ query }: { query: QueryState<DatasetAiInsightResponse> }) {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
            <Brain className="h-5 w-5" />
          </div>
          <div>
            <CardTitle>AI insights</CardTitle>
            <CardDescription>Cached AI insight snapshot generated from deterministic analysis context.</CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {query.isLoading ? <LoadingInline label="Loading AI insights" /> : null}
        {query.isError ? <SectionError title="Insights unavailable" messages={getApiErrorMessages(query.error, "AI insights could not be loaded.")} /> : null}
        {query.data ? (
          <div className="grid gap-4 xl:grid-cols-[1.1fr_1fr_1fr]">
            <div className="rounded-lg border bg-background p-4">
              <div className="flex items-center justify-between gap-3">
                <p className="font-medium">Summary</p>
                <Badge variant={query.data.generationStatus === "GENERATED" ? "default" : "warning"}>{query.data.generationStatus}</Badge>
              </div>
              <p className="mt-3 text-sm leading-6 text-muted-foreground">{query.data.datasetDescription}</p>
              <p className="mt-4 text-xs text-muted-foreground">
                Model {query.data.modelName} / Generated {formatDate(query.data.generatedAt)}
              </p>
              {query.data.errorMessage ? <p className="mt-3 text-sm text-destructive">{query.data.errorMessage}</p> : null}
            </div>
            <InsightList title="Potential issues" items={query.data.potentialIssues} />
            <div className="grid gap-4">
              <InsightList title="Suggested analyses" items={query.data.suggestedAnalyses} />
              <InsightList title="Suggested visualizations" items={query.data.suggestedVisualizations} />
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}

function ChartPanel({
  title,
  description,
  children,
}: {
  title: string;
  description: string;
  children: ReactNode;
}) {
  return (
    <div className="rounded-lg border bg-background p-4">
      <div className="mb-4">
        <p className="font-medium">{title}</p>
        <p className="mt-1 text-sm text-muted-foreground">{description}</p>
      </div>
      {children}
    </div>
  );
}

function ChartEmpty({ message }: { message: string }) {
  return (
    <div className="flex h-64 items-center justify-center rounded-lg border border-dashed bg-card p-6 text-center text-sm text-muted-foreground">
      {message}
    </div>
  );
}

function ColumnProfileCard({ column }: { column: ColumnProfile }) {
  const total = column.nullCount + column.nonNullCount;
  return (
    <div className="rounded-lg border bg-background p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate font-medium">{column.columnName}</p>
          <p className="mt-1 text-xs text-muted-foreground">Position {column.columnPosition + 1}</p>
        </div>
        <Badge variant="outline">{column.inferredDataType}</Badge>
      </div>
      <div className="mt-4 grid grid-cols-3 gap-2 text-sm">
        <ProfileMetric label="Null" value={formatNumber(column.nullCount)} />
        <ProfileMetric label="Non-null" value={formatNumber(column.nonNullCount)} />
        <ProfileMetric label="Unique" value={formatNumber(column.uniqueCount)} />
      </div>
      <div className="mt-4">
        <p className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground">Common values</p>
        <div className="mt-2 space-y-2">
          {column.mostCommonValues.length > 0 ? (
            column.mostCommonValues.map((item) => (
              <div key={`${item.value}-${item.count}`} className="flex items-center justify-between gap-3 text-sm">
                <span className="truncate text-muted-foreground">{item.value || "Empty"}</span>
                <span className="font-medium">{item.count}</span>
              </div>
            ))
          ) : (
            <p className="text-sm text-muted-foreground">No common values returned.</p>
          )}
        </div>
      </div>
      <p className="mt-4 text-xs text-muted-foreground">{formatNumber(total)} preview values evaluated</p>
    </div>
  );
}

function IssueList({ issues, emptyText }: { issues: QualityIssueSummary[]; emptyText: string }) {
  if (issues.length === 0) {
    return <p className="rounded-lg border bg-background p-4 text-sm text-muted-foreground">{emptyText}</p>;
  }

  return (
    <div className="space-y-2">
      {issues.map((issue, index) => (
        <div key={`${issue.type}-${index}`} className="rounded-lg border bg-background p-3">
          <Badge variant="secondary">{issue.type.replace(/_/g, " ")}</Badge>
          <p className="mt-2 text-sm text-muted-foreground">{issue.message}</p>
        </div>
      ))}
    </div>
  );
}

function InsightList({ title, items }: { title: string; items: string[] }) {
  return (
    <div className="rounded-lg border bg-background p-4">
      <p className="font-medium">{title}</p>
      {items.length > 0 ? (
        <ul className="mt-3 space-y-2 text-sm text-muted-foreground">
          {items.map((item) => (
            <li key={item} className="flex gap-2">
              <span className="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-primary" />
              <span>{item}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="mt-3 text-sm text-muted-foreground">No items returned.</p>
      )}
    </div>
  );
}

function SummaryMetric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border bg-background p-4">
      <p className="text-muted-foreground">{label}</p>
      <p className="mt-1 font-semibold">{value}</p>
    </div>
  );
}

function ProfileMetric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md border bg-card p-3">
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className="mt-1 font-medium">{value}</p>
    </div>
  );
}

function LoadingPanel({ title, description }: { title: string; description: string }) {
  return (
    <section className="flex min-h-40 items-center justify-center rounded-lg border bg-card p-6 shadow-panel">
      <div className="text-center">
        <Loader2 className="mx-auto h-8 w-8 animate-spin text-primary" />
        <p className="mt-3 font-medium">{title}</p>
        <p className="mt-1 text-sm text-muted-foreground">{description}</p>
      </div>
    </section>
  );
}

function LoadingInline({ label }: { label: string }) {
  return (
    <div className="flex min-h-32 items-center justify-center rounded-lg border bg-background">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Loader2 className="h-4 w-4 animate-spin text-primary" />
        {label}
      </div>
    </div>
  );
}

function EmptyPanel({ title, description }: { title: string; description: string }) {
  return (
    <div className="rounded-lg border border-dashed bg-background p-6 text-center">
      <FileText className="mx-auto h-8 w-8 text-muted-foreground" />
      <p className="mt-3 font-medium">{title}</p>
      <p className="mt-1 text-sm text-muted-foreground">{description}</p>
    </div>
  );
}

function FeedbackPanel({ tone, title, messages }: { tone: "success" | "error"; title: string; messages: string[] }) {
  const toneClasses =
    tone === "success" ? "border-primary/30 bg-primary/10 text-primary" : "border-destructive/30 bg-destructive/10 text-destructive";
  const Icon = tone === "success" ? CheckCircle2 : AlertCircle;

  return (
    <div className={`rounded-lg border p-4 text-sm ${toneClasses}`}>
      <div className="flex items-start gap-3">
        <Icon className="mt-0.5 h-4 w-4 shrink-0" />
        <div>
          <p className="font-medium">{title}</p>
          {messages.map((message) => (
            <p key={message} className="mt-1">
              {message}
            </p>
          ))}
        </div>
      </div>
    </div>
  );
}

function SectionError({ title, messages }: { title: string; messages: string[] }) {
  return (
    <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
      <div className="flex items-start gap-3">
        <AlertCircle className="mt-0.5 h-4 w-4" />
        <div>
          <p className="font-medium">{title}</p>
          {messages.map((message) => (
            <p key={message} className="mt-1">
              {message}
            </p>
          ))}
          <Button asChild className="mt-3" variant="outline" size="sm">
            <Link to="/datasets">Back to datasets</Link>
          </Button>
        </div>
      </div>
    </div>
  );
}

function StatusBadge({ status }: { status: DatasetStatus }) {
  const variant = status === "READY" ? "default" : status === "FAILED" ? "warning" : status === "UPLOADED" ? "outline" : "secondary";
  return <Badge variant={variant}>{status.replace(/_/g, " ")}</Badge>;
}

function formatNumber(value: number) {
  return new Intl.NumberFormat().format(value);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
}

function formatEnumLabel(value: string) {
  return value
    .replace(/_/g, " ")
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function formatFileSize(bytes: number) {
  if (bytes === 0) {
    return "0 B";
  }

  const units = ["B", "KB", "MB", "GB"];
  const unitIndex = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  const value = bytes / 1024 ** unitIndex;
  return `${value.toFixed(value >= 10 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
}

function isNotFoundError(error: Error) {
  return error instanceof AxiosError && error.response?.status === 404;
}

type QueryState<T> = {
  data?: T;
  error: Error | null;
  isError: boolean;
  isLoading: boolean;
};
