# AI Index

This file is the fastest entry point for AI assistants and maintainers.

## Read Order by Task

- Dependency and build updates:
  1. `README.md`
  2. `gradle/libs.versions.toml`
  3. `build-logic/BUILD_LOGIC_BEST_PRACTICES.md`
  4. `docs/release-policy.md`
- Architecture and module boundaries:
  1. `ARCHITECTURE.md`
  2. `docs/module-map.md`
  3. `settings.gradle.kts`
  4. `docs/module-standards.md`
- Replicate existing architecture/data flows:
  1. `docs/ai/AGENT_PLAYBOOK.md`
  2. `docs/ai/ARCHITECTURE_GUARDRAILS.md`
  3. `docs/ai/FLOW_CATALOG.md`
  4. `docs/ai/PATTERN_RECIPES.md`
- CI, release, and governance:
  1. `docs/governance.md`
  2. `docs/release-policy.md`
  3. `docs/play-release.md`
  4. `.github/workflows/*.yml`
  5. `doppler.yaml` + `scripts/`
- Runtime services and cloud dependencies:
  1. `docs/cloud.md`
  2. `docs/library-audit.md`
- Visual design and UX:
  1. `docs/design.md`

## Where To Edit

- User-facing project overview: `README.md`
- Team process and policy: `docs/*.md`
- AI and automation context: `AGENTS.md`, `ARCHITECTURE.md`, `AI_INDEX.md`
- Module inventory: `docs/module-map.md`
- Cloud/secrets context: `docs/cloud.md`
- AI execution and replication guides: `docs/ai/*.md`

## Non-Goals

- Do not store secrets in docs.
- Do not duplicate source-of-truth values when they already exist in code or build files.
