import { Link } from "react-router-dom";
import { Database, FileUp, MoreHorizontal } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

const datasets = [
  {
    id: "customer-churn",
    name: "Customer Churn Export",
    owner: "analytics@dataforge.local",
    rows: "50 preview rows",
    status: "READY",
    quality: "94.2",
  },
  {
    id: "revenue-sample",
    name: "Revenue Sample",
    owner: "finance@dataforge.local",
    rows: "50 preview rows",
    status: "UPLOADED",
    quality: "88.7",
  },
  {
    id: "operations-metrics",
    name: "Operations Metrics",
    owner: "ops@dataforge.local",
    rows: "36 preview rows",
    status: "METADATA_CREATED",
    quality: "Pending",
  },
];

export function DatasetsPage() {
  return (
    <div className="space-y-6">
      <section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 className="text-2xl font-semibold tracking-normal">Datasets</h2>
          <p className="mt-1 text-sm text-muted-foreground">Manage uploaded CSV assets and review their profiling readiness.</p>
        </div>
        <Button>
          <FileUp />
          Upload CSV
        </Button>
      </section>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <Database className="h-5 w-5" />
            </div>
            <div>
              <CardTitle>Dataset inventory</CardTitle>
              <CardDescription>Placeholder inventory table prepared for `GET /api/datasets`.</CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="overflow-hidden rounded-lg border">
            <div className="hidden grid-cols-[1.6fr_1.2fr_1fr_1fr_80px] gap-4 border-b bg-muted px-4 py-3 text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground md:grid">
              <span>Name</span>
              <span>Owner</span>
              <span>Rows</span>
              <span>Quality</span>
              <span></span>
            </div>
            {datasets.map((dataset) => (
              <div
                key={dataset.id}
                className="grid gap-3 border-b bg-card px-4 py-4 last:border-b-0 md:grid-cols-[1.6fr_1.2fr_1fr_1fr_80px] md:items-center"
              >
                <div>
                  <Link className="font-medium text-foreground hover:text-primary" to={`/datasets/${dataset.id}`}>
                    {dataset.name}
                  </Link>
                  <div className="mt-2">
                    <Badge variant={dataset.status === "READY" ? "default" : "secondary"}>{dataset.status}</Badge>
                  </div>
                </div>
                <p className="text-sm text-muted-foreground">{dataset.owner}</p>
                <p className="text-sm text-muted-foreground">{dataset.rows}</p>
                <p className="text-sm font-medium">{dataset.quality}</p>
                <Button variant="ghost" size="icon">
                  <MoreHorizontal />
                </Button>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
