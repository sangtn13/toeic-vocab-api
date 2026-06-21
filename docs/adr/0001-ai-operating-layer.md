# ADR 0001: Add an AI Operating Layer

Date: 2026-06-20
Status: Accepted

## Context

As the app grows, agents can break unrelated behavior when they edit from incomplete context or reread too much code inefficiently. The system spans a Spring Boot backend and a Next.js frontend, so FE-BE contract drift is a recurring risk.

## Decision

Add a small AI operating layer to the backend repo:

- `AGENTS.md` for required agent rules.
- `CONTEXT.md` for compact backend memory.
- `docs/agents/*` for engineering-skill configuration.
- `docs/ai/*` for module maps, contract snapshots, and durable rules.
- `docs/specs/*` for spec-driven changes.

This mirrors the frontend AI operating layer in `D:\toeic-vocab-app`.

## Consequences

Agents should read less code but read better context first. Any durable change to domain language, API contract, module ownership, or rules must update the relevant docs. The frontend remains in a separate repo, so backend docs reference `D:\toeic-vocab-app` and its AI/API docs.
