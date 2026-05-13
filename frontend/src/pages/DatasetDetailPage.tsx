import { useParams } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

const columns = [
  { name: "customer_id", type: "TEXT", nulls: "0%", uniqueness: "100%" },
  { name: "monthly_spend", type: "DECIMAL", nulls: "2%", uniqueness: "84%" },
  { name: "signup_date", type: "DATE", nulls: "0%", uniqueness: "76%" },
  { name: "is_active", type: "BOOLEAN", nulls: "4%", uniqueness: "2%" },
];

export function DatasetDetailPage() {
  const { datasetId } = useParams();

  return (
    <div className="space-y-6">
      <section className="rounded-lg border bg-card p-6 shadow-panel">
        <div className="flex flex-col justify-between gap-4 lg:flex-row lg:items-start">
          <div>
            <Badge variant="default">Dataset detail</Badge>
            <h2 className="mt-4 text-2xl font-semibold tracking-normal">Customer Churn Export</h2>
            <p className="mt-2 max-w-3xl text-sm leading-6 text-muted-foreground">
              Shell view for dataset `{datasetId}`. Endpoint-specific preview, profile, quality, and insight wiring can be added incrementally.
            </p>
          </div>
          <div className="grid grid-cols-3 gap-3 text-sm">
            <div className="rounded-lg border bg-background p-4">
              <p className="text-muted-foreground">Status</p>
              <p className="mt-1 font-semibold">READY</p>
            </div>
            <div className="rounded-lg border bg-background p-4">
              <p className="text-muted-foreground">Quality</p>
              <p className="mt-1 font-semibold">94.2</p>
            </div>
            <div className="rounded-lg border bg-background p-4">
              <p className="text-muted-foreground">Columns</p>
              <p className="mt-1 font-semibold">4</p>
            </div>
          </div>
        </div>
      </section>

      <section className="grid gap-4 xl:grid-cols-[1.2fr_1fr]">
        <Card>
          <CardHeader>
            <CardTitle>Column profile summary</CardTitle>
            <CardDescription>Static structure aligned to the backend profile response shape.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="overflow-hidden rounded-lg border">
              {columns.map((column) => (
                <div key={column.name} className="grid gap-3 border-b bg-card px-4 py-4 last:border-b-0 sm:grid-cols-4 sm:items-center">
                  <p className="font-medium">{column.name}</p>
                  <Badge variant="outline">{column.type}</Badge>
                  <p className="text-sm text-muted-foreground">Nulls {column.nulls}</p>
                  <p className="text-sm text-muted-foreground">Unique {column.uniqueness}</p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Insight readiness</CardTitle>
            <CardDescription>Prepared for cached AI insight snapshots without adding chat behavior.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="rounded-lg border bg-background p-4">
              <p className="text-sm font-medium">Deterministic context</p>
              <p className="mt-1 text-sm text-muted-foreground">Profile and quality results are available before insight generation.</p>
            </div>
            <div className="rounded-lg border bg-background p-4">
              <p className="text-sm font-medium">Fallback behavior</p>
              <p className="mt-1 text-sm text-muted-foreground">The backend can return unavailable status when Ollama is offline.</p>
            </div>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
