# 30-60-90 Day Checklist

Use this after a major release or when onboarding to the project.

## First 30 Days

- Enable required branch protection checks in GitHub using CI workflow job names.
- Enable Dependabot auto-updates for Gradle dependencies and Actions.
- Stabilize CI runtime and verify artifact visibility.
- Review AI context docs: `AI_INDEX.md`, `AGENTS.md`, `docs/cloud.md`.

## Days 31–60

- Reduce `build.maxIssues` in `config/detekt/detekt.yml` by a safe increment.
- Close top recurring Detekt findings in touched modules.
- Run monthly dependency report and create issues for high-value updates.
- Publish first maintenance release with a complete `CHANGELOG.md` entry.

## Days 61–90

- Review quality KPI trends (coverage delta, Detekt findings trend, CI success rate).
- Remove or shrink Detekt baselines where feasible.
- Re-validate AI context docs against current module graph and cloud setup.
- Plan next quarter roadmap: new features, tech debt, dependency upgrades.
