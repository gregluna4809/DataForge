# DataForge Roadmap

## Project Vision

DataForge aims to become:

> A modern AI-assisted analytics and data quality platform that combines deterministic profiling with local AI interpretation.

The architecture emphasizes:

- explainability
- ownership security
- deterministic analytics
- modular engineering
- AI augmentation instead of AI replacement

---

# Completed Phases

## Phase 1 — Backend Foundation ✅

Completed:

- Spring Boot backend
- PostgreSQL integration
- Flyway migrations
- JWT authentication
- User ownership model
- Structured API error handling
- Integration tests

---

## Phase 2 — Data Ingestion ✅

Completed:

- CSV upload infrastructure
- Local file storage
- Multipart upload endpoint
- CSV validation
- UUID-safe filenames
- CSV parsing
- Preview extraction
- Persistent preview storage

---

## Phase 3 — Analytics Engine ✅

Completed:

- Dataset profiling
- Type inference
- Null/non-null analysis
- Unique value analysis
- Most common value detection
- Profile persistence

---

## Phase 4 — Data Quality Engine ✅

Completed:

- Dataset-level quality scoring
- Column-level quality scoring
- High null-rate detection
- Low uniqueness detection
- Empty column detection
- Identifier-column detection
- Quality issue summaries

---

## Phase 5 — AI Insight Layer ✅

Completed:

- Ollama integration
- Structured prompt generation
- AI-generated dataset summaries
- Suggested analyses
- Suggested visualizations
- Cached AI insight persistence
- Graceful unavailable fallback

---

# Next Recommended Milestones

## Phase 6 — Frontend Dashboard

Planned:

- React + TypeScript frontend
- Authentication UI
- Dataset upload interface
- Dataset dashboard
- Preview table UI
- Profiling cards/charts
- Quality score dashboard
- AI insights panel
- Responsive layout

---

## Phase 7 — Advanced Profiling

Planned:

- Full-file profiling
- Chunked processing
- Streaming analytics
- Duplicate detection
- Outlier detection
- Statistical summaries
- Column correlations
- Date/time inference

---

## Phase 8 — AI Analytics Expansion

Planned:

- Natural-language querying
- AI-generated chart recommendations
- SQL generation
- Data cleaning recommendations
- Automated anomaly explanations
- Conversational dataset analysis

---

## Phase 9 — Enterprise Features

Planned:

- Async processing jobs
- Upload processing queue
- Progress tracking
- Audit logging
- Dataset versioning
- Role-based access control
- Cloud storage support
- Multi-user collaboration

---

## Phase 10 — Deployment & Infrastructure

Planned:

- Docker support
- Production deployment profiles
- CI/CD pipeline
- Monitoring & logging
- Health dashboards
- Cloud deployment
- Reverse proxy setup

---

# Current Architecture

```text
JWT Authentication
    ↓
Dataset Ownership
    ↓
CSV Upload Infrastructure
    ↓
Preview Parsing Layer
    ↓
Profiling Engine
    ↓
Quality Scoring Engine
    ↓
AI Insight Layer
```

---

# Development Workflow

Recommended workflow:

```text
Prompt
→ Agent implementation
→ Human review
→ Manual validation
→ Commit stable milestone
→ Next modular feature
```

This project intentionally follows incremental architectural growth rather than large uncontrolled feature generation.

---

# Long-Term Goals

DataForge is intended to demonstrate:

- backend engineering
- data engineering
- AI-assisted analytics
- enterprise architecture
- modular software design
- secure multi-user systems
- deterministic analytics + AI augmentation

The long-term vision is a portfolio-quality analytics platform suitable for:

- government technology
- enterprise analytics
- AI-assisted data tooling
- data quality engineering
- analytics engineering
- modern data platforms

