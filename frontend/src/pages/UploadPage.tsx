import { ChangeEvent, DragEvent, FormEvent, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AlertCircle, CheckCircle2, FileText, Loader2, UploadCloud, X } from "lucide-react";
import { createDataset, uploadDatasetCsv } from "@/api/datasets";
import { getApiErrorMessages } from "@/api/errors";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";
import type { CreateDatasetRequest, DatasetUploadResponse } from "@/types/datasets";

const initialFormState = {
  name: "",
  description: "",
  rowCount: "0",
  columnCount: "0",
};

type UploadStep = "idle" | "creating" | "uploading" | "success" | "error";

export function UploadPage() {
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [form, setForm] = useState(initialFormState);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [errors, setErrors] = useState<string[]>([]);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [step, setStep] = useState<UploadStep>("idle");
  const [uploadResult, setUploadResult] = useState<DatasetUploadResponse | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  const isBusy = step === "creating" || step === "uploading";
  const derivedName = useMemo(() => {
    if (form.name.trim()) {
      return form.name;
    }

    return selectedFile?.name.replace(/\.csv$/i, "") ?? "";
  }, [form.name, selectedFile]);

  const uploadMutation = useMutation({
    mutationFn: async () => {
      if (!selectedFile) {
        throw new Error("Select a CSV file before uploading.");
      }

      setStep("creating");
      setUploadProgress(0);

      const metadataRequest: CreateDatasetRequest = {
        name: derivedName.trim(),
        originalFilename: selectedFile.name,
        description: form.description.trim() ? form.description.trim() : null,
        rowCount: Number(form.rowCount),
        columnCount: Number(form.columnCount),
        fileSizeBytes: selectedFile.size,
      };

      const dataset = await createDataset(metadataRequest);

      setStep("uploading");
      return uploadDatasetCsv(dataset.id, selectedFile, (event) => {
        if (!event.total) {
          return;
        }

        setUploadProgress(Math.round((event.loaded * 100) / event.total));
      });
    },
    onSuccess: async (response) => {
      setStep("success");
      setUploadResult(response);
      setUploadProgress(100);
      setErrors([]);
      setForm(initialFormState);
      setSelectedFile(null);
      await queryClient.invalidateQueries({ queryKey: ["datasets"] });
    },
    onError: (error) => {
      setStep("error");
      setErrors(getApiErrorMessages(error, error instanceof Error ? error.message : "CSV upload failed."));
    },
  });

  function updateForm(field: keyof typeof initialFormState, value: string) {
    setForm((currentForm) => ({ ...currentForm, [field]: value }));
  }

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    selectFile(file);
    event.target.value = "";
  }

  function selectFile(file: File | null) {
    setErrors([]);
    setUploadResult(null);
    setStep("idle");
    setUploadProgress(0);

    if (!file) {
      setSelectedFile(null);
      return;
    }

    if (!file.name.toLowerCase().endsWith(".csv")) {
      setSelectedFile(null);
      setErrors(["Only .csv files are supported."]);
      return;
    }

    setSelectedFile(file);
    setForm((currentForm) => ({
      ...currentForm,
      name: currentForm.name || file.name.replace(/\.csv$/i, ""),
    }));
  }

  function handleDrop(event: DragEvent<HTMLDivElement>) {
    event.preventDefault();
    setIsDragging(false);
    selectFile(event.dataTransfer.files?.[0] ?? null);
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrors([]);

    if (!selectedFile) {
      setErrors(["Select a CSV file before starting the upload."]);
      return;
    }

    if (!derivedName.trim()) {
      setErrors(["Dataset name is required."]);
      return;
    }

    uploadMutation.mutate();
  }

  return (
    <div className="space-y-6">
      <section className="flex flex-col justify-between gap-4 lg:flex-row lg:items-center">
        <div>
          <h2 className="text-2xl font-semibold tracking-normal">Upload CSV</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            Create dataset metadata, then upload the CSV file through the protected backend upload endpoint.
          </p>
        </div>
        <Button asChild variant="outline">
          <Link to="/datasets">View datasets</Link>
        </Button>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1fr_380px]">
        <Card>
          <CardHeader>
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <UploadCloud className="h-5 w-5" />
              </div>
              <div>
                <CardTitle>CSV upload workflow</CardTitle>
                <CardDescription>Metadata is created first; file upload starts after the dataset record exists.</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <form className="space-y-5" onSubmit={handleSubmit}>
              <div
                className={cn(
                  "flex min-h-56 cursor-pointer flex-col items-center justify-center rounded-lg border border-dashed bg-background p-6 text-center transition-colors",
                  isDragging && "border-primary bg-primary/5",
                  selectedFile && "border-primary/50 bg-primary/5",
                )}
                onClick={() => fileInputRef.current?.click()}
                onDragOver={(event) => {
                  event.preventDefault();
                  setIsDragging(true);
                }}
                onDragLeave={() => setIsDragging(false)}
                onDrop={handleDrop}
              >
                <input ref={fileInputRef} className="hidden" type="file" accept=".csv,text/csv" onChange={handleFileChange} />
                <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-card shadow-panel">
                  <FileText className="h-6 w-6 text-primary" />
                </div>
                {selectedFile ? (
                  <div className="mt-4">
                    <p className="font-medium">{selectedFile.name}</p>
                    <p className="mt-1 text-sm text-muted-foreground">{formatFileSize(selectedFile.size)} selected</p>
                    <Button
                      className="mt-4"
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={(event) => {
                        event.stopPropagation();
                        selectFile(null);
                      }}
                      disabled={isBusy}
                    >
                      <X />
                      Remove file
                    </Button>
                  </div>
                ) : (
                  <div className="mt-4">
                    <p className="font-medium">Drop a CSV file here or browse</p>
                    <p className="mt-1 text-sm text-muted-foreground">Client-side validation accepts `.csv` files only.</p>
                  </div>
                )}
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="upload-name">Dataset name</Label>
                  <Input
                    id="upload-name"
                    value={form.name}
                    onChange={(event) => updateForm("name", event.target.value)}
                    disabled={isBusy}
                    maxLength={150}
                    placeholder="Customer export"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="upload-description">Description</Label>
                  <Input
                    id="upload-description"
                    value={form.description}
                    onChange={(event) => updateForm("description", event.target.value)}
                    disabled={isBusy}
                    maxLength={1000}
                    placeholder="Optional context"
                  />
                </div>
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="upload-row-count">Initial row count</Label>
                  <Input
                    id="upload-row-count"
                    type="number"
                    min="0"
                    value={form.rowCount}
                    onChange={(event) => updateForm("rowCount", event.target.value)}
                    disabled={isBusy}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="upload-column-count">Initial column count</Label>
                  <Input
                    id="upload-column-count"
                    type="number"
                    min="0"
                    value={form.columnCount}
                    onChange={(event) => updateForm("columnCount", event.target.value)}
                    disabled={isBusy}
                    required
                  />
                </div>
              </div>

              {isBusy ? (
                <div className="rounded-lg border bg-background p-4">
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-medium">{step === "creating" ? "Creating dataset metadata" : "Uploading CSV file"}</span>
                    <span className="text-muted-foreground">{step === "creating" ? "Step 1 of 2" : `${uploadProgress}%`}</span>
                  </div>
                  <div className="mt-3 h-2 overflow-hidden rounded-full bg-secondary">
                    <div
                      className="h-full rounded-full bg-primary transition-all"
                      style={{ width: `${step === "creating" ? 35 : uploadProgress}%` }}
                    />
                  </div>
                </div>
              ) : null}

              {errors.length > 0 ? (
                <StatusMessage tone="error" messages={errors} />
              ) : null}

              {step === "success" && uploadResult ? (
                <StatusMessage
                  tone="success"
                  messages={[
                    `${uploadResult.originalFilename} uploaded successfully.`,
                    `Dataset status: ${uploadResult.status.replace(/_/g, " ")}`,
                  ]}
                />
              ) : null}

              <Button className="w-full" type="submit" disabled={isBusy}>
                {isBusy ? (
                  <>
                    <Loader2 className="animate-spin" />
                    {step === "creating" ? "Creating metadata..." : "Uploading CSV..."}
                  </>
                ) : (
                  <>
                    <UploadCloud />
                    Create metadata and upload CSV
                  </>
                )}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Upload checklist</CardTitle>
            <CardDescription>What this workflow handles in the current milestone.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <ChecklistItem label="Authenticated request" complete />
            <ChecklistItem label="Dataset metadata created first" complete />
            <ChecklistItem label="Multipart CSV upload" complete />
            <ChecklistItem label="Dataset list refreshed after success" complete />
            <div className="rounded-lg border bg-background p-4">
              <Badge variant="secondary">Preview not wired yet</Badge>
              <p className="mt-3 text-sm leading-6 text-muted-foreground">
                Upload triggers backend parsing, profiling, and scoring, but preview/profile/quality UI pages remain intentionally out of scope.
              </p>
            </div>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}

function StatusMessage({ tone, messages }: { tone: "success" | "error"; messages: string[] }) {
  const isSuccess = tone === "success";
  const Icon = isSuccess ? CheckCircle2 : AlertCircle;
  return (
    <div
      className={cn(
        "rounded-md border p-3 text-sm",
        isSuccess ? "border-primary/30 bg-primary/10 text-primary" : "border-destructive/30 bg-destructive/10 text-destructive",
      )}
    >
      <div className="flex items-start gap-3">
        <Icon className="mt-0.5 h-4 w-4" />
        <div>
          {messages.map((message) => (
            <p key={message}>{message}</p>
          ))}
        </div>
      </div>
    </div>
  );
}

function ChecklistItem({ label, complete }: { label: string; complete: boolean }) {
  return (
    <div className="flex items-center gap-3 rounded-lg border bg-background p-3">
      <div className={cn("flex h-7 w-7 items-center justify-center rounded-md", complete ? "bg-primary/10 text-primary" : "bg-secondary")}>
        <CheckCircle2 className="h-4 w-4" />
      </div>
      <p className="text-sm font-medium">{label}</p>
    </div>
  );
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
