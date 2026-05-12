# DataForge

DataForge is a full-stack AI-assisted data profiling and analytics platform built with Java 21, Spring Boot, PostgreSQL, Flyway, and Ollama.

The platform allows authenticated users to upload CSV datasets, preview parsed data, generate profiling statistics, calculate dataset quality metrics, and generate AI-assisted insights using deterministic analytics as the foundation.

---

# Core Philosophy

DataForge follows a deterministic-first architecture.

The system performs:
- authenticated ingestion
- structured parsing
- profiling
- quality scoring
- persistent analytics

before introducing AI interpretation.

AI is used as an augmentation layer on top of reliable deterministic analytics rather than replacing core data engineering logic.

---

# Current Features

## Authentication & Security

- JWT authentication
- BCrypt password hashing
- Stateless Spring Security configuration
- User registration and login
- Protected API endpoints
- Dataset ownership enforcement
- Cross-user access rejection

## Dataset Management

- Dataset metadata persistence
- User-owned datasets
- UUID identifiers
- Dataset status tracking
- Upload timestamp tracking

## CSV Upload Infrastructure

- Multipart CSV upload endpoint
- File type validation (.csv)
- Local file storage
- UUID-safe stored filenames
- Configurable upload directory
- Structured upload error handling

## CSV Preview Parsing

- Apache Commons CSV parsing
- Header extraction
- First 50 preview rows
- Quoted value handling
- Embedded comma handling
- Missing value handling
- Persistent preview row storage

## Dataset Profiling Engine

Per-column analytics:

- null count
- non-null count
- unique count
- inferred data types
- most common values

## Dataset Quality Scoring

Dataset-level and per-column scoring:

- null/empty detection
- low uniqueness detection
- possible identifier column detection
- text/unknown type warnings
- quality issue summaries

## AI Insight Layer

- Ollama integration
- Deterministic prompt generation
- Dataset summaries
- Suggested analyses
- Suggested visualizations
- Graceful fallback if Ollama is unavailable
- Persistent AI insight snapshots

---

# Technology Stack

## Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Flyway
- Maven
- PostgreSQL
- Apache Commons CSV

## AI

- Ollama
- Local LLM inference

## Testing

- JUnit
- Spring Security Test
- Integration testing

---

# API Overview

## Authentication

### Register

```http
POST /api/auth/register
```

### Login

```http
POST /api/auth/login
```

---

## Datasets

### List Datasets

```http
GET /api/datasets
```

### Create Dataset Metadata

```http
POST /api/datasets
```

### Upload CSV

```http
POST /api/datasets/{datasetId}/upload
```

### Dataset Preview

```http
GET /api/datasets/{datasetId}/preview
```

### Dataset Profile

```http
GET /api/datasets/{datasetId}/profile
```

### Dataset Quality

```http
GET /api/datasets/{datasetId}/quality
```

### Dataset AI Insights

```http
GET /api/datasets/{datasetId}/insights
```

---

# Local Development

## Requirements

- Java 21
- PostgreSQL 16+
- Ollama

---

## PostgreSQL Setup

```sql
CREATE USER dataforge WITH PASSWORD 'root123';
CREATE DATABASE dataforge OWNER dataforge;
GRANT ALL PRIVILEGES ON DATABASE dataforge TO dataforge;
```

---

## Run Backend

```powershell
cd backend
$env:DATAFORGE_DB_PASSWORD="root123"
.\mvnw.cmd spring-boot:run
```

---

# Environment Variables

## Database

```text
DATAFORGE_DB_URL
DATAFORGE_DB_USERNAME
DATAFORGE_DB_PASSWORD
```

## Ollama

```text
DATAFORGE_OLLAMA_ENDPOINT
DATAFORGE_OLLAMA_MODEL
DATAFORGE_OLLAMA_TIMEOUT_SECONDS
```

---

# Design Principles

- Modular architecture
- Deterministic analytics first
- AI augmentation second
- Layered backend structure
- Incremental feature growth
- Production-style organization
- Explicit service boundaries
- UUID-based identifiers
- Ownership-based authorization

---

# Current Status

## Completed

- Backend foundation
- JWT authentication
- Dataset ownership
- CSV upload infrastructure
- CSV preview parsing
- Dataset profiling engine
- Dataset quality scoring
- Ollama AI insight layer
- Integration tests
- Flyway migrations

