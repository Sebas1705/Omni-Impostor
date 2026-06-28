# Design — Omni-Impostor

This document describes the current design architecture of the Omni-Impostor app, serving as reference for improvements and redesigns.

## 1. General Concept

The app is a digital board game designed for groups. The design must be **fun, intuitive, and modern**, enabling fast gameplay with minimal friction. It is built on **Material Design 3 (M3)** with custom touches to give it a playful character.

## 2. Visual Identity

### Color Palette (Core UI)

The theme uses Material 3 with support for three contrast levels (Low, Medium, High) and light/dark mode.

- **Primary:** Blue (`#1A5FB5` light / `#A0C8FF` dark) — main action and interaction color.
- **Secondary:** Green (`#1C6B47` light / `#98D8AB` dark) — confirmation, civilians, positive elements.
- **Tertiary:** Yellow (`#785A00` light / `#FBC21F` dark) — warnings, tension, special emphasis.
- **Error:** Standard M3 Red (`#BA1A1A`) — errors and impostor roles.
- **Backgrounds:**
  - *Light mode:* Cool neutral white (`#F8F9FF`).
  - *Dark mode:* Cool dark grey (`#111318`) with surfaces at `#161A22`.

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

- **Icon Consistency:** Improve category icons so they feel part of a unified icon pack.
- **Visual Feedback:** Enhance transitions between game phases to feel more dynamic and alive.
- **Results Hierarchy:** Make the winners screen more visually impactful and celebratory.
- **Accessibility:** Ensure all color contrast levels comply with WCAG 2.1 AA at minimum.
