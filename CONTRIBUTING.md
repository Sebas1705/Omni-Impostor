# Contributing Guide

## Scope

This repository contains the **Omni-Impostor** Android app and its CI/CD automation in `.github/`.

## Getting Started

1. Fork and clone the repository.
2. Set up secrets (requires Doppler CLI + `doppler login`):
   ```bash
   doppler setup          # auto-selects omni-impostor/dev_personal
   bash scripts/setup-secrets.sh   # or .\scripts\setup-secrets.ps1 on Windows
   ```
3. Create a branch from `develop` (features) or `main` (hotfixes).
4. Run checks locally:
   ```powershell
   .\gradlew.bat assembleDevDebug
   .\gradlew.bat detekt
   .\gradlew.bat coverageUnitTestAll
   ```

## Branch Strategy (GitFlow)

| Branch | Purpose |
|---|---|
| `main` | Production releases only, always tagged |
| `develop` | Integration branch — all features merge here first |
| `feature/*` | New features, branched from `develop` |
| `release/*` | Release prep, branched from `develop`, merged to `main` + `develop` |
| `hotfix/*` | Critical fixes, branched from `main`, merged to `main` + `develop` |

## Pull Request Rules

- Open PRs against `develop` (features) or `main` (hotfixes).
- Link the related issue when applicable.
- Keep changes scoped; explain migration impact for breaking changes.
- Update docs in the same PR when behavior changes.
- Apply at least one label: `bug`, `enhancement`, `documentation`, `ci`, `security`, or `breaking-change`.
- Ensure CI is green before requesting review.

## Word lists

Browse the data in `docs/word-map/index.html` and fix small things with `node scripts/words/edit.mjs` (see `docs/word-map/README.md`).
The words are authored as a concept map in `data/files/concepts/{concepts,links}.jsonl` (design:
`docs/studies/word-collection/concept-map-spec.md`). The app bundles these two files as they are and compiles the playable
words (clues ranked from hints and links) on the device, per language and category, the first time a game needs them
(`ConceptCompiler`). There are no generated word lists to refresh: edit the concepts and commit. To see what the app will
compile, and to refresh the browsable viewer, run:

```powershell
node scripts/words/compile.mjs
```

`ConceptCompiler.kt` is a port of `compileAll` in `scripts/words/lib.mjs`. Both are tested against the same fixture
(`scripts/words/fixtures/graph`): if you change a compile rule, change both and run `node scripts/words/compile.mjs --golden`
to rewrite the fixture's expected lists. CI runs `compile.mjs --check`, `ConceptSourcesValidationTest` (shape of the
sources), `ConceptCompilerParityTest` (Kotlin equals the fixture) and `CompiledWordsValidationTest` (shape of the compiled
words: at most 4 words, at least 3 distinct clues, no clue equal to the word, no duplicates in a category).

Clue ratings from players: with your own Google credentials run `node scripts/words/ratings.mjs fetch <file>` (see its header),
then `ratings.mjs import <file>` and `compile.mjs`; `ratings.mjs report` lists the best and worst rated clues. `node --test scripts/words/words.test.mjs`
runs the tooling tests.

Proposals (AI-written Spanish labels and clue types) live in `data/files/concepts/proposals/` and are not part of the map
until reviewed: `node scripts/words/proposals.mjs review <file>` shows a batch, delete a line to reject it or edit a value to
correct it, then `node scripts/words/proposals.mjs apply <file>` merges it and `compile.mjs` regenerates the lists.

## Commit Style

Short scoped messages:
- `feat(offlinegame): ...`
- `fix(auth): ...`
- `chore(ci): ...`
- `docs: ...`
- `build-logic: ...`

## Code of Conduct

Be respectful, constructive, and collaborative in issues and pull requests.
