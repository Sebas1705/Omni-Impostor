# Visual refresh (offline-only app) — design

Status: draft for review · Date: 2026-10-18 · Platform: Android (Jetpack Compose, Material 3)

## Goal

The online mode is gone (see PR "remove online mode leftovers"), so the app is a single offline party game.
Use that to give the whole app a more polished, consistent look **without changing its identity**:
keep the blue/green palette, the three fonts (Finger Paint titles, Gupter body, Raleway display) and the three contrast levels.

Non-goals: new game rules, new features, changes to navigation structure, new fonts or a new palette,
changes to data/domain layers.

## Current state (what we are improving)

- `core/ui` already has the M3 theme (`Color.kt`, `Type.kt`, `Shapes.kt`, `Theme.kt`) with light/dark and 3 contrast levels.
- There is no shared spacing/elevation scale: screens hardcode `dp` values (`ResultComponents`, `RankingDesign`,
  `FaceDesign`, `LoginDesign`, `ProfileDesign` have the most) and `RoundedCornerShape(...)` is repeated inline.
- `core/ui` has only one reusable component (`GameBackground`); each feature builds its own cards and chips.
- Category icons (`CategoryIcons.kt`) mix unrelated Material icons (e.g. `Book`, `MenuBook`, `AutoStories`, `Description`).
- `docs/design.md` is outdated: it says tertiary is yellow, the code uses blue/teal tones.
- `docs/design.md` already lists the pending improvements: icon consistency, phase transitions, results hierarchy,
  WCAG AA contrast.

## Approach

Incremental, one PR per phase, each phase leaves the app shippable. Shared pieces first, then screens use them.

### Phase 0 — Visual foundation (`core/ui`)

1. **Tokens** (`ui/theme/Dimens.kt`): `Spacing` (4, 8, 12, 16, 24, 32 dp), `Elevation` (0, 1, 3, 6 dp) exposed through
   a `LocalSpacing`-style `CompositionLocal` in `AppTheme`, so previews and tests can override it.
   `appShapes` stays; add a `CardShape` alias to the existing `large` shape instead of inline shapes.
2. **Base components** (`ui/components/`), only the ones at least two screens need:
   - `AppCard` (surface container + optional header icon/title, optional selected state, consistent padding/shape/elevation).
   - `SectionHeader` (title in Finger Paint + optional subtitle).
   - `StatusChip` (ready / warning / error variants, replaces ad-hoc chips such as the "game ready" chips).
   - `PrimaryActionButton` (full-width filled tonal button with the click sound already used in the app).
3. **Contrast audit**: unit test in `core/ui` that computes WCAG 2.1 contrast ratios for the key pairs
   (`onX` over `X` for primary, secondary, tertiary, error, surface, background) in all 6 schemes and asserts AA (4.5:1).
   Any failing pair is fixed in `Color.kt` (smallest change to the color value). This is the only place colors may change.
4. **Icon set**: keep Material Icons Extended `Outlined` (no new dependency), define one icon per category with a
   consistent metaphor, and fix the inconsistent ones in `CategoryIcons.kt`.
5. Update `docs/design.md` so it matches the code (palette, tokens, components).

Acceptance: build + detekt green, contrast test green, no visual change to game logic, `docs/design.md` updated.

### Phase 1 — Home and game setup

`FaceDesign` (already simplified by the online cleanup), categories grid, players, mode picker, readiness chips.
Use `AppCard`/`SectionHeader`/`StatusChip`/`PrimaryActionButton`, better hierarchy for the "start game" area,
tablet layout (two columns, already present) kept and aligned to the tokens.

### Phase 2 — Game flow

Reveal, Discussion, Result, Rating screens. Smoother transitions between phases (`AnimatedContent`), a clearer
discussion timer (color change in the last 30 s and 10 s, matching the existing alerts), and a more celebratory result
screen with a clear winner hierarchy. Reveal keeps the 3D flip card.
Depends on the discussion-timer fix (PR #1) so the timer states can be seen in practice.

### Phase 3 — Ranking and Profile

Podium, table and stats cards on the shared components. Ranking is now a single view (no tabs); Profile keeps its tabs.

### Phase 4 — Settings, Login, Splash

Settings dialog cards, login layout, splash branding. Final consistency pass over spacing/shape usage
(search for leftover hardcoded `dp`/`RoundedCornerShape`).

## Cross-cutting rules

- Accessibility: touch targets ≥ 48 dp, `contentDescription` on meaningful icons, text must survive 200 % font scale
  without clipping, AA contrast in all three contrast levels.
- Must work in light/dark, es/en, phone and tablet width (the existing `>= 600.dp` split layouts).
- No new third-party dependencies. No changes outside `core/ui`, `core/resources` (strings) and feature UI packages.
- Dev builds and debug tooling are not part of the redesign.

## Testing and verification

- Phase 0: contrast unit test; existing unit tests keep passing.
- Every phase: `:app:compileDevDebugKotlin`, detekt on touched modules, and manual screenshots in an emulator
  (phone + tablet, light/dark, es/en, contrast levels low/high) attached to the PR description.
  There are no UI/screenshot tests in the project; adding a screenshot-testing library is out of scope (YAGNI).
- CI is currently stopped by a GitHub billing problem, so local verification is the gate until it is fixed.

## Risks

- Hardcoded `dp` and inline shapes are spread over many files; phases 1–4 touch them gradually, so the app is
  temporarily mixed (old and new spacing) between PRs. Mitigation: each PR converts whole screens, not parts.
- Contrast fixes may slightly alter colors in some schemes; they are limited to failing pairs and listed in the PR.
- Without screenshot tests, visual regressions are caught only by manual review.

## Open points (to decide while implementing, not blockers)

- Exact spacing values after seeing the first converted screen (the 4/8/12/16/24/32 scale is a starting point).
- Whether `PrimaryActionButton` is worth keeping as a component or just a style, depending on how many call sites need it.
