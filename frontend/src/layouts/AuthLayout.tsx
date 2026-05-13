import { Outlet } from "react-router-dom";

export function AuthLayout() {
  return (
    <main className="min-h-screen bg-background">
      <div className="grid min-h-screen lg:grid-cols-[1fr_520px]">
        <section className="hidden border-r bg-card lg:block">
          <div className="flex h-full flex-col justify-between p-10">
            <div>
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary text-sm font-semibold text-primary-foreground">
                  DF
                </div>
                <div>
                  <p className="text-sm font-semibold">DataForge</p>
                  <p className="text-xs text-muted-foreground">Data quality intelligence</p>
                </div>
              </div>
              <div className="mt-24 max-w-xl">
                <p className="text-sm font-medium text-primary">Enterprise analytics foundation</p>
                <h1 className="mt-4 text-4xl font-semibold leading-tight tracking-normal">
                  Govern uploaded datasets with deterministic profiling and quality scoring.
                </h1>
                <p className="mt-5 text-base leading-7 text-muted-foreground">
                  DataForge keeps ingestion, profiling, quality checks, and AI summaries grounded in auditable backend results.
                </p>
              </div>
            </div>
            <div className="grid grid-cols-3 gap-4 text-sm">
              <div className="rounded-lg border bg-background p-4">
                <p className="font-semibold">JWT</p>
                <p className="mt-1 text-muted-foreground">Protected access</p>
              </div>
              <div className="rounded-lg border bg-background p-4">
                <p className="font-semibold">CSV</p>
                <p className="mt-1 text-muted-foreground">Structured preview</p>
              </div>
              <div className="rounded-lg border bg-background p-4">
                <p className="font-semibold">Quality</p>
                <p className="mt-1 text-muted-foreground">Profile-backed scoring</p>
              </div>
            </div>
          </div>
        </section>
        <section className="flex items-center justify-center px-6 py-10">
          <Outlet />
        </section>
      </div>
    </main>
  );
}
