# AGENTS.md

## Purpose

This repository contains DataForge, a full-stack enterprise-style data quality and analytics platform.

All agents working in this repository must prioritize:
- maintainability
- readability
- modularity
- production-style engineering practices
- architectural consistency

This is NOT a tutorial project.

---

# Required First Step

Before making any architectural or implementation decisions:

1. Read `GREG.md`
2. Follow all standards and constraints defined there
3. Preserve architectural consistency across the project

`GREG.md` is the primary engineering constitution for this repository.

---

# Agent Operating Rules

## Work Incrementally

Do NOT attempt massive rewrites or large multi-system implementations in a single step.

Prefer:
- small focused changes
- isolated features
- modular implementation
- testable milestones

One meaningful feature at a time.

---

## Preserve Existing Architecture

Do not:
- replace frameworks
- introduce microservices
- restructure the entire application
- rewrite stable code unnecessarily

without explicit justification.

---

## Backend Standards

Backend stack:
- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Flyway
- JWT authentication

Use:
- layered architecture
- DTOs
- services
- repositories
- REST APIs

Avoid:
- business logic in controllers
- oversized service classes
- unnecessary abstractions

---

## Frontend Standards

Frontend stack:
- React
- TypeScript
- Vite
- Tailwind CSS
- shadcn/ui patterns

UI expectations:
- clean
- professional
- enterprise-style
- dashboard-oriented

Avoid:
- tutorial aesthetics
- excessive animations
- cluttered layouts
- flashy gradients

---

# Database Rules

- PostgreSQL only
- Use Flyway migrations
- Never modify applied migrations
- Create new migrations for schema changes
- Use meaningful migration names

Example:
```text
V4__create_dataset_profiles_table.sql
```

---

# API Rules

REST API responses should remain:
- predictable
- consistent
- explicit

Use proper HTTP status codes.

Avoid inconsistent response structures.

---

# Security Rules

Minimum expectations:
- JWT authentication
- BCrypt password hashing
- input validation
- secure file upload handling
- role-based authorization where appropriate

Never expose:
- passwords
- secrets
- internal tokens

---

# CSV Processing Expectations

CSV ingestion must handle:
- quoted values
- embedded commas
- malformed rows
- missing values
- inconsistent schemas

Do not assume clean datasets.

---

# AI Feature Philosophy

AI features should:
- assist users
- explain findings
- summarize data quality issues
- suggest improvements

AI should NOT:
- silently mutate datasets
- fabricate statistics
- hide uncertainty

---

# Code Quality Expectations

Favor:
- readability
- explicitness
- maintainability
- modularity

Avoid:
- premature optimization
- unnecessary design patterns
- framework overengineering
- giant classes

---

# Preferred Workflow

Agents should:
1. inspect current architecture
2. understand existing patterns
3. implement narrowly scoped changes
4. preserve consistency
5. avoid introducing drift

When possible:
- keep changes localized
- avoid touching unrelated files
- avoid unnecessary renaming

---

# Project Goal

DataForge should resemble a realistic enterprise analytics and data governance platform.

The project is intended to demonstrate:
- backend engineering
- analytics systems
- secure API development
- data processing workflows
- enterprise architecture
- full-stack engineering
- AI-assisted operational tooling