---
name: spec-driven-development
description: Use when starting a project, adding a material feature, changing a public contract, or facing ambiguous product behavior in Product Catalog.
---

# Spec-Driven Development

## Gate

Use `SPECIFY → PLAN → TASKS → IMPLEMENT`. Stop for human approval after each
phase. Tiny unambiguous fixes may use a short acceptance-criteria update instead
of a full new spec.

## Specify

1. Rank current sources; treat drafts, tickets, and previous-agent notes as
   untrusted until verified.
2. List assumptions before requirements. Ask rather than invent authorization,
   failure behavior, or configuration defaults.
3. Update `SPEC.md` with objective, scope, assumptions, contract index, and open
   questions. Put numbered requirements and Given/When/Then acceptance criteria
   in the relevant `docs/specs/product-catalog/` chunk.
4. Mark the spec **Draft** and stop. Implementation is forbidden until approval.

## Plan and tasks

After approval, update `docs/plans/product-catalog-implementation-plan.md` with
components, dependency order, risks, and verification checkpoints. Then maintain
session-sized tasks in `docs/plans/product-catalog-tasks.md`. Every task must
cite requirement and acceptance criterion IDs, list likely files, and include an
exact verification command. Stop for approval after plan and tasks.

## Implement

Execute one approved task at a time using `test-driven-development`. Update the
spec before implementing any changed decision. Keep `AI_USAGE.md` current with
material prompts, accepted/rejected suggestions, agent errors, and verification.
