# AGENTS

Operating contract for AI agents and automation helpers working in this project.

## Mission

- Keep Omni-Impostor stable, maintainable, and well-documented.
- Prefer incremental changes with verifiable outputs.

## Core Rules

- Respect module boundaries (`core`, `data`, `domain`, `feature`).
- Prefer convention plugins and version catalog over ad-hoc Gradle configuration.
- Keep debug-only tooling in debug variants.
- Update docs in the same change when behavior or process changes.

## Required Checks Before Finishing

- Compile affected modules.
- Run detekt on affected modules.
- If dependencies or build logic changed, run `dependencyUpdates --no-parallel`.

## Safe Change Workflow

1. Read context from `AI_INDEX.md` and the relevant docs.
2. Apply the minimum necessary edits.
3. Validate with targeted Gradle tasks.
4. Summarize what changed, what was verified, and any known constraints.

## Architecture and Flow Replication Docs

- Playbook: `docs/ai/AGENT_PLAYBOOK.md`
- Guardrails: `docs/ai/ARCHITECTURE_GUARDRAILS.md`
- Existing flows: `docs/ai/FLOW_CATALOG.md`
- Recipes: `docs/ai/PATTERN_RECIPES.md`

## Do Not

- Do not commit secrets or credentials.
- Do not change release/governance rules silently.
- Do not rename or move modules without updating `settings.gradle.kts` and all cross-module documentation.

