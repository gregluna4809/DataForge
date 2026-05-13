import { FormEvent, useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AlertCircle, Database, FileText, Loader2, Plus, RefreshCw } from "lucide-react";
import { createDataset, getDatasets } from "@/api/datasets";
import { getApiErrorMessages } from "@/api/errors";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { CreateDatasetRequest, Dataset, DatasetStatus } from "@/types/datasets";

const initialFormState = {
  name: "",
  originalFilename: "",
  description: "",
  rowCount: "0",
  columnCount: "0",
  fileSizeBytes: "0",
};

type DatasetFormState = typeof initialFormState;

export function DatasetsPage() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState<DatasetFormState>(initialFormState);
  const [formErrors, setFormErrors] = useState<string[]>([]);

  const datasetsQuery = useQuery({
    queryKey: ["datasets"],
    queryFn: getDatasets,
  });

  const createDatasetMutation = useMutation({
    mutationFn: createDataset,
    onSuccess: async () => {
      setForm(initialFormState);
      setFormErrors([]);
      await queryClient.invalidateQueries({ queryKey: ["datasets"] });
    },
    onError: (error) => {
      setFormErrors(getApiErrorMessages(error, "Unable to create dataset metadata."));
    },
  });

  function updateForm(field: keyof DatasetFormState, value: string) {
    setForm((currentForm) => ({ ...currentForm, [field]: value }));
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormErrors([]);

    const request: CreateDatasetRequest = {
      name: form.name.trim(),
      originalFilename: form.originalFilename.trim(),
      description: form.description.trim() ? form.description.trim() : null,
      rowCount: Number(form.rowCount),
      columnCount: Number(form.columnCount),
      fileSizeBytes: Number(form.fileSizeBytes),
    };

    createDatasetMutation.mutate(request);
  }

  const datasets = datasetsQuery.data ?? [];

  return (
    <div className="space-y-6">
      <section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 className="text-2xl font-semibold tracking-normal">Datasets</h2>
          <p className="mt-1 text-sm text-muted-foreground">Manage uploaded CSV assets and review their profiling readiness.</p>
        </div>
        <Button variant="outline" onClick={() => datasetsQuery.refetch()} disabled={datasetsQuery.isFetching}>
          <RefreshCw className={datasetsQuery.isFetching ? "animate-spin" : undefined} />
          Refresh
        </Button>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1fr_380px]">
        <Card>
          <CardHeader>
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <Database className="h-5 w-5" />
              </div>
              <div>
                <CardTitle>Dataset inventory</CardTitle>
                <CardDescription>Authenticated metadata from `GET /api/datasets`.</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {datasetsQuery.isLoading ? <DatasetLoadingState /> : null}

            {datasetsQuery.isError ? (
              <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                <div className="flex items-start gap-3">
                  <AlertCircle className="mt-0.5 h-4 w-4" />
                  <div>
                    <p className="font-medium">Unable to load datasets</p>
                    <p className="mt-1">{getApiErrorMessages(datasetsQuery.error, "Dataset inventory could not be loaded.").join(" ")}</p>
                  </div>
                </div>
              </div>
            ) : null}

            {!datasetsQuery.isLoading && !datasetsQuery.isError && datasets.length === 0 ? <DatasetEmptyState /> : null}

            {!datasetsQuery.isLoading && !datasetsQuery.isError && datasets.length > 0 ? (
              <div className="overflow-hidden rounded-lg border">
                <div className="hidden grid-cols-[1.3fr_1.2fr_0.7fr_0.7fr_0.8fr_1fr] gap-4 border-b bg-muted px-4 py-3 text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground lg:grid">
                  <span>Dataset</span>
                  <span>Filename</span>
                  <span>Rows</span>
                  <span>Columns</span>
                  <span>File size</span>
                  <span>Uploaded</span>
                </div>
                {datasets.map((dataset) => (
                  <DatasetRow key={dataset.id} dataset={dataset} />
                ))}
              </div>
            ) : null}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/20 text-accent-foreground">
                <Plus className="h-5 w-5" />
              </div>
              <div>
                <CardTitle>Create metadata</CardTitle>
                <CardDescription>Create a dataset record before CSV upload is added.</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={handleSubmit}>
              <div className="space-y-2">
                <Label htmlFor="dataset-name">Dataset name</Label>
                <Input
                  id="dataset-name"
                  value={form.name}
                  onChange={(event) => updateForm("name", event.target.value)}
                  disabled={createDatasetMutation.isPending}
                  maxLength={150}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="original-filename">Original filename</Label>
                <Input
                  id="original-filename"
                  value={form.originalFilename}
                  onChange={(event) => updateForm("originalFilename", event.target.value)}
                  disabled={createDatasetMutation.isPending}
                  maxLength={255}
                  placeholder="customers.csv"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <textarea
                  id="description"
                  className="min-h-24 w-full rounded-md border border-input bg-card px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                  value={form.description}
                  onChange={(event) => updateForm("description", event.target.value)}
                  disabled={createDatasetMutation.isPending}
                  maxLength={1000}
                  placeholder="Optional context for this dataset"
                />
              </div>
              <div className="grid gap-3 sm:grid-cols-3 xl:grid-cols-1 2xl:grid-cols-3">
                <div className="space-y-2">
                  <Label htmlFor="row-count">Rows</Label>
                  <Input
                    id="row-count"
                    type="number"
                    min="0"
                    value={form.rowCount}
                    onChange={(event) => updateForm("rowCount", event.target.value)}
                    disabled={createDatasetMutation.isPending}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="column-count">Columns</Label>
                  <Input
                    id="column-count"
                    type="number"
                    min="0"
                    value={form.columnCount}
                    onChange={(event) => updateForm("columnCount", event.target.value)}
                    disabled={createDatasetMutation.isPending}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="file-size">Bytes</Label>
                  <Input
                    id="file-size"
                    type="number"
                    min="0"
                    value={form.fileSizeBytes}
                    onChange={(event) => updateForm("fileSizeBytes", event.target.value)}
                    disabled={createDatasetMutation.isPending}
                    required
                  />
                </div>
              </div>

              {formErrors.length > 0 ? (
                <div className="rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
                  {formErrors.map((message) => (
                    <p key={message}>{message}</p>
                  ))}
                </div>
              ) : null}

              <Button className="w-full" type="submit" disabled={createDatasetMutation.isPending}>
                {createDatasetMutation.isPending ? (
                  <>
                    <Loader2 className="animate-spin" />
                    Creating metadata...
                  </>
                ) : (
                  <>
                    <Plus />
                    Create dataset
                  </>
                )}
              </Button>
            </form>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}

function DatasetRow({ dataset }: { dataset: Dataset }) {
  return (
    <div className="grid gap-3 border-b bg-card px-4 py-4 last:border-b-0 lg:grid-cols-[1.3fr_1.2fr_0.7fr_0.7fr_0.8fr_1fr] lg:items-center">
      <div>
        <Link className="font-medium text-foreground hover:text-primary" to={`/datasets/${dataset.id}`}>
          {dataset.name}
        </Link>
        <div className="mt-2 flex flex-wrap items-center gap-2">
          <StatusBadge status={dataset.status} />
          <span className="text-xs text-muted-foreground">{dataset.uploadedBy.email}</span>
        </div>
      </div>
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <FileText className="h-4 w-4" />
        <span className="truncate">{dataset.originalFilename}</span>
      </div>
      <MetadataValue label="Rows" value={formatNumber(dataset.rowCount)} />
      <MetadataValue label="Columns" value={formatNumber(dataset.columnCount)} />
      <MetadataValue label="File size" value={formatFileSize(dataset.fileSizeBytes)} />
      <MetadataValue label="Uploaded" value={formatDate(dataset.fileUploadedAt ?? dataset.uploadTimestamp)} />
    </div>
  );
}

function MetadataValue({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-[0.12em] text-muted-foreground lg:hidden">{label}</p>
      <p className="text-sm text-foreground lg:text-muted-foreground">{value}</p>
    </div>
  );
}

function StatusBadge({ status }: { status: DatasetStatus }) {
  const variant = status === "READY" ? "default" : status === "FAILED" ? "warning" : status === "UPLOADED" ? "outline" : "secondary";
  return <Badge variant={variant}>{status.replace(/_/g, " ")}</Badge>;
}

function DatasetLoadingState() {
  return (
    <div className="flex min-h-64 items-center justify-center rounded-lg border bg-background">
      <div className="text-center">
        <Loader2 className="mx-auto h-8 w-8 animate-spin text-primary" />
        <p className="mt-3 text-sm font-medium">Loading datasets</p>
        <p className="mt-1 text-sm text-muted-foreground">Fetching your authenticated dataset inventory.</p>
      </div>
    </div>
  );
}

function DatasetEmptyState() {
  return (
    <div className="flex min-h-64 items-center justify-center rounded-lg border border-dashed bg-background p-6 text-center">
      <div className="max-w-md">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10 text-primary">
          <Database className="h-6 w-6" />
        </div>
        <h3 className="mt-4 text-base font-semibold">No datasets yet</h3>
        <p className="mt-2 text-sm leading-6 text-muted-foreground">
          Create a metadata record to establish ownership and prepare the dataset for a future CSV upload step.
        </p>
      </div>
    </div>
  );
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

function formatFileSize(bytes: number) {
  if (bytes === 0) {
    return "0 B";
  }

  const units = ["B", "KB", "MB", "GB"];
  const unitIndex = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  const value = bytes / 1024 ** unitIndex;
  return `${value.toFixed(value >= 10 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
}
