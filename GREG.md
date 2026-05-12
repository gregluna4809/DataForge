# GREG.md

# Gregory Luna Engineering Constitution

This document defines the engineering philosophy, architectural standards, workflow expectations, and implementation constraints for the DataForge project.

All contributors, agents, and AI-assisted tooling should follow these standards unless explicitly directed otherwise.

---

# Project Identity

Project Name:
DataForge

Project Type:
Enterprise-style data quality, analytics, and governance platform.

Primary Goal:
Build a realistic full-stack analytics platform that demonstrates:
- backend engineering
- enterprise architecture
- data processing workflows
- analytics systems
- secure API development
- modular software design
- AI-assisted operational tooling

This project should resemble a realistic internal enterprise platform rather than a tutorial application.

---

# Core Engineering Philosophy

## Modularity First

Do not attempt to build the entire system in massive implementation passes.

Prefer:
- small features
- isolated changes
- incremental improvements
- focused milestones
- reversible commits

One meaningful feature at a time.

Large uncontrolled generations increase:
- architectural drift
- technical debt
- inconsistency
- hidden defects

---

## Readability Over Cleverness

Favor:
- explicit code
- understandable logic
- maintainable structure
- predictable behavior

Avoid:
- magic abstractions
- unnecessary metaprogramming
- premature optimization
- overly clever patterns

The project should be understandable by a future engineer reading the code six months later.

---

## Enterprise Realism

All implementation decisions should prioritize:
- maintainability
- scalability
- operational realism
- production-style patterns
- security
- consistency

Avoid tutorial-style shortcuts.

---

# Technology Stack

## Backend

Required backend stack:
- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Flyway
- Spring Security
- JWT authentication

Preferred architecture:
- monolithic backend
- layered architecture
- DTO-based API boundaries
- REST APIs

Microservices are NOT required.

---

## Frontend

Required frontend stack:
- React
- TypeScript
- Vite
- Tailwind CSS
- shadcn/ui-style patterns

UI expectations:
- clean
- modern
- enterprise-style
- dashboard-oriented
- responsive

Avoid:
- childish styling
- excessive animations
- cluttered layouts
- flashy gradients
- crypto-startup aesthetics

---

# Database Philosophy

## PostgreSQL Standardization

DataForge uses PostgreSQL exclusively.

Use PostgreSQL-compatible syntax and assumptions.

Do NOT introduce:
- MySQL-specific syntax
- SQLite assumptions
- incompatible ORM shortcuts

---

## Migration Rules

Flyway is required.

Rules:
- never modify applied migrations
- create new migrations instead
- use descriptive migration names
- preserve migration history integrity

Example:
```text
V5__create_quality_reports_table.sql
```

---

# Package Organization

Preferred backend package structure:

```text
com.dataforge
├── auth
├── users
├── datasets
├── ingestion
├── profiling
├── rules
├── reports
├── ai
├── security
└── common
```

Avoid dumping unrelated code into generic utility folders.

---

# Backend Standards

## Controllers

Controllers should:
- validate requests
- delegate work
- return responses

Controllers should NOT contain:
- business logic
- large processing routines
- data transformation pipelines

---

## Services

Business logic belongs in services and domain components.

Services should remain:
- focused
- modular
- testable

Avoid massive “god classes.”

---

## DTO Philosophy

Use DTOs to separate:
- API contracts
- persistence models
- internal implementation details

Avoid exposing entities directly through APIs.

---

# API Design Standards

REST APIs should remain:
- explicit
- predictable
- consistent

Use:
- proper HTTP status codes
- structured error responses
- stable naming conventions

---

## Error Response Structure

Preferred API error format:

```json
{
  "timestamp": "2026-05-11T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/datasets"
}
```

---

# Security Standards

Minimum expectations:
- JWT authentication
- BCrypt password hashing
- CORS configuration
- input validation
- file upload validation
- authorization checks

Never expose:
- passwords
- secrets
- internal tokens
- sensitive implementation details

---

# CSV Processing Philosophy

CSV ingestion must assume:
- malformed files
- missing values
- embedded commas
- quoted values
- inconsistent schemas
- unexpected encodings

Do not assume datasets are clean.

Large-file scalability should be considered from the beginning.

---

# AI Integration Philosophy

AI features exist to:
- assist users
- explain findings
- summarize issues
- suggest improvements

AI should NOT:
- silently modify datasets
- fabricate metrics
- hide uncertainty
- replace deterministic validation

All AI-generated insights should remain explainable.

---

# Logging Philosophy

Logging should support:
- debugging
- observability
- operational analysis

Log:
- uploads
- authentication events
- processing milestones
- failures
- rule execution summaries

Do NOT log:
- passwords
- secrets
- tokens
- sensitive user data

---

# Docker Philosophy

Docker is optional during early-stage development.

Avoid unnecessary infrastructure complexity.

Prefer:
- local Java installation
- local PostgreSQL
- local Node.js

Docker should be introduced only where it provides meaningful operational value.

---

# Agent Workflow Philosophy

Agents should:
1. inspect the current architecture
2. understand existing conventions
3. preserve consistency
4. make focused changes
5. avoid unnecessary rewrites

Agents should NOT:
- replace frameworks unexpectedly
- introduce major architectural shifts
- rewrite stable systems
- change unrelated files unnecessarily

---

# Git Workflow Philosophy

Prefer:
- small commits
- meaningful commit messages
- reversible milestones
- incremental development

Never continue building on top of a broken application state.

If the project fails to:
- compile
- boot
- build
- run tests

then stabilize the system before continuing development.

---

# Long-Term Vision

DataForge should evolve into a lightweight enterprise data governance and quality intelligence platform capable of:
- operational analytics
- quality monitoring
- anomaly detection
- validation workflows
- AI-assisted reporting
- audit support
- scalable dataset analysis

The final system should demonstrate strong software engineering discipline and realistic enterprise architecture practices.