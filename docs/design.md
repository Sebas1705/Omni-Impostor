# Design — Omni-Impostor

This document describes the current design architecture of the Omni-Impostor app, serving as reference for improvements and redesigns.

## 1. General Concept

The app is a digital board game designed for groups. The design must be **fun, intuitive, and modern**, enabling fast gameplay with minimal friction. It is built on **Material Design 3 (M3)** with custom touches to give it a playful character.

## 2. Visual Identity

### Color Palette (Core UI)

The theme uses Material 3 with three contrast levels (Low, Medium, High) in light and dark mode
(six schemes in `core/ui/.../theme/Color.kt` and `Theme.kt`). Low-contrast light values:

- **Primary:** Blue (`#1565C0`) — main action and interaction color.
- **Secondary:** Teal (`#00796B`) — confirmation, civilians, positive elements.
- **Tertiary:** Light blue (`#0277BD`) — secondary emphasis.
- **Error:** Red (`#BA1A1A`) — errors and impostor roles.
- **Background (light):** cool white (`#F7FAFF`).

`ContrastTest` (core/ui) fails if any key foreground/background pair of any scheme drops below WCAG AA (4.5:1).

### Design tokens and base components

- Spacing (`AppDimens.spacing`): xs 4, sm 8, md 12, lg 16, xl 24, xxl 32 dp.
- Elevation (`AppDimens.elevation`): none 0, low 1, medium 3, high 6 dp.
- Shapes: `appShapes` (4/8/12/16/24 dp); cards use `MaterialTheme.cardShape` (the `large` shape).
- Components in `core/ui/.../components`: `AppCard`, `SectionHeader`, `StatusChip`, `PrimaryActionButton`.
  New screens should use them instead of inline `dp` values and `RoundedCornerShape(...)`.
- Category icons: one distinct Material `Outlined` icon per category (`CategoryIcons.kt`, guarded by `CategoryIconsTest`).

### Typography

Google Fonts are used to differentiate information hierarchy:

- **Titles:** `Finger Paint` — A hand-painted style font that reinforces the casual tone of the game.
- **Body:** `Gupter` — For clear, professional reading in text blocks.
- **Display / Headers:** `Raleway` — For a modern, clean look on labels and buttons.

## 3. User Experience (UX)

### Navigation Flow

1. **Splash:** Loading screen with animated logo.
2. **Login:** User identity management.
3. **Home (Dashboard):** Central access to play, profile, ranking, and settings.
4. **Game Setup:**
   - Category selection (icon card grid).
   - Player management (dynamic list).
   - Game mode selection (variant picker).
5. **Game Cycle:**
   - **Reveal:** Critical phase where each player privately discovers their word via an **animated card with 3D rotation** triggered by drag or tap.
   - **Discussion:** Timed debate screen with countdown.
   - **Result:** Visual summary of winners and losers with score cards.

### Key Components

- **Reveal Card:** Card with pronounced shadow and Y-axis rotation animation (0 to 180 degrees).
- **Categories:** Interactive cards with clear visual selection states.
- **Buttons:** Extensive use of rounded-corner buttons (M3) with primary container colors.

## 4. Areas for Improvement

- ~~**Icon Consistency**~~ Done in the visual foundation (phase 0).
- **Visual Feedback:** Enhance transitions between game phases to feel more dynamic and alive.
- **Results Hierarchy:** Make the winners screen more visually impactful and celebratory.
- ~~**Accessibility (contrast)**~~ Guarded by `ContrastTest`; touch targets and font scaling are checked per phase.

Roadmap: `docs/designs/2026-10-18-visual-refresh-design.md`.
