# Agent Playbook

Practical execution guide for AI agents working on Omni-Impostor.

## Goal

Make safe, repeatable changes that preserve architecture boundaries and existing data flows.

## Mandatory Read Order

1. `AI_INDEX.md`
2. `AGENTS.md`
3. `ARCHITECTURE.md`
4. `docs/ai/ARCHITECTURE_GUARDRAILS.md`
5. `docs/ai/FLOW_CATALOG.md`
6. `docs/ai/PATTERN_RECIPES.md`

## Definition of Done

- Change respects module boundaries.
- Existing flow pattern is reused (or deviation is documented).
- Affected modules compile.
- Detekt runs on affected modules.
- Docs are updated in the same PR when behavior/process changes.

## Work Protocol

1. Identify closest existing flow in `docs/ai/FLOW_CATALOG.md`.
2. Copy the matching recipe from `docs/ai/PATTERN_RECIPES.md`.
3. Apply minimal edits in the correct layer.
4. Validate with targeted Gradle tasks.
5. Report: what changed, what was validated, what remains constrained.

## Common Mistakes to Avoid

- Injecting `data` implementations directly into `feature`.
- Putting UI logic in `domain`.
- Adding debug libraries without variant scoping.
- Changing root/subfolder workflow paths without updating docs.

