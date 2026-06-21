# Spec-Driven Workflow

Use specs to keep backend and frontend changes coherent without rereading both repos end-to-end.

## When to create a spec

Create or update a spec under `docs/specs/` when a change:

- Touches both backend and frontend.
- Changes API contract, DTOs, auth, progress, or cache behavior.
- Adds a backend use case that needs controller/service/DTO/test changes.
- Changes persistence schema or migrations.
- Refactors module ownership or durable architecture.
- Has risk of breaking an existing public study, auth, or admin flow.

For tiny docs-only or local implementation changes, a spec is optional.

## Workflow

1. Name the spec: `docs/specs/YYYY-MM-DD-short-feature-name.md`.
2. Write the problem, goals, non-goals, touched modules, and FE-BE contract impact.
3. List invariants that must not break.
4. Implement the smallest vertical slice first.
5. Update frontend contract/types/services/hooks in the same slice when needed.
6. Update the spec as decisions change.
7. Before finishing, mark verification and remaining risks.

## Required sections

Use `docs/specs/_template.md` as the starting point.

Keep specs concise. A useful spec is a map, not a novel.

