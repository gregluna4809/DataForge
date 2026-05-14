# DataForge Roadmap

## Project Vision

DataForge aims to become:

> A modern AI-assisted analytics and data quality platform combining deterministic profiling, rule-based quality enforcement, and local AI-assisted interpretation.

Core architectural principles:

- explainability
- ownership security
- deterministic analytics
- modular engineering
- AI augmentation instead of blind AI autonomy
- local-first privacy-aware AI integration

---

# Completed Phases

## Phase 1 — Backend Foundation ✅

Completed:

- Spring Boot backend
- PostgreSQL integration
- Flyway migrations
- JWT authentication
- ownership-based dataset access control
- structured API exception handling
- integration and service testing

---

## Phase 2 — Data Ingestion ✅

Completed:

- CSV upload infrastructure
- multipart upload API
- local file storage
- UUID-safe stored filenames
- CSV validation
- CSV parsing
- preview extraction
- preview persistence

---

## Phase 3 — Analytics Engine ✅

Completed:

- dataset profiling
- column type inference
- null/non-null analysis
- uniqueness analysis
- common value detection
- profile persistence

---

## Phase 4 — Data Quality Engine ✅

Completed:

- dataset quality scoring
- column-level quality scoring
- high null-rate detection
- low uniqueness detection
- empty column detection
- identifier heuristics
- issue summaries

---

## Phase 5 — Frontend Analytics Platform ✅

Completed:

- React + TypeScript frontend
- JWT authentication UI
- dataset registration/upload interface
- dataset dashboard
- dataset detail pages
- preview table UI
- profiling cards
- quality score dashboard
- Recharts visual analytics
- AI insights panel
- responsive application shell

---

## Phase 6 — Deterministic Cleaning Engine ✅

Completed:

- deterministic CSV cleaning pipeline
- whitespace trimming
- blank normalization
- header normalization
- snake_case header transformation
- empty row removal
- duplicate row removal
- cleaned CSV generation
- cleaning report persistence
- cleaned CSV download
- ownership-secured cleaning endpoints

---

## Phase 7 — AI Analytics Layer ✅

Completed:

- Ollama local AI integration
- resilient Java HTTP client integration
- model fallback handling
- cached AI insight generation
- graceful unavailable fallback
- conversational dataset analyst chat
- session-based conversational memory
- grounded prompt construction
- dataset-aware contextual Q&A

---

# Current Active Milestones

## Phase 8 — Data Quality Intelligence Engine

Planned:

- deterministic duplicate row detection
- duplicate identifier detection
- malformed email detection
- invalid numeric detection
- invalid date detection
- boolean inconsistency detection
- categorical inconsistency detection
- structured issue classification
- issue severity ranking

Target API:

```text
GET /api/datasets/{id}/issues
```

Goal:

Move issue detection from LLM inference into deterministic backend logic.

---

## Phase 9 — AI-Assisted Data Operations

Planned:

- AI issue-aware recommendations
- categorical normalization suggestions
- boolean cleanup proposals
- date normalization suggestions
- numeric coercion suggestions
- transformation preview
- one-click transformation execution

Example workflow:

```text
Detected:
Paid
paid
PAID
PAid

Suggested normalization:
PAID

Apply?
```

---

## Phase 10 — Advanced Analytics

Planned:

- full-file profiling
- chunked processing
- streaming analytics
- statistical summaries
- outlier detection
- column correlations
- date/time intelligence
- anomaly detection
- trend analysis

---

## Phase 11 — Enterprise Platform Features

Planned:

- async processing jobs
- progress tracking
- audit logging
- dataset versioning
- role-based access control
- cloud storage abstraction
- multi-user collaboration
- administrative observability

---

## Phase 12 — Deployment & Infrastructure

Planned:

- Docker support
- production deployment profiles
- CI/CD pipeline
- structured monitoring
- application metrics
- reverse proxy deployment
- cloud deployment support

---

# Current Architecture

```text
JWT Authentication
    ↓
Ownership Security Layer
    ↓
CSV Upload / Storage Layer
    ↓
Preview Extraction Layer
    ↓
Profiling Engine
    ↓
Quality Scoring Engine
    ↓
Deterministic Cleaning Engine
    ↓
AI Insight Layer
    ↓
Conversational Dataset Analyst
```

---

# Development Workflow

```text
Architect feature
→ implement modularly
→ human validation
→ deterministic verification
→ commit stable milestone
→ advance roadmap
```

---

# Long-Term Goals

DataForge is intended to demonstrate:

- backend engineering
- frontend engineering
- data engineering
- AI-assisted analytics
- enterprise architecture
- secure multi-user systems
- deterministic analytics + local AI augmentation
- intelligent data quality operations

Portfolio positioning:

- government technology
- enterprise analytics
- AI-assisted data tooling
- data quality engineering
- analytics engineering
- modern data platforms