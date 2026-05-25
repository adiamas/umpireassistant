# Umpire Assistant

An Android app for baseball, softball, and kickball officials. It tracks pitch counts, runs, and game state during a game.

## Screenshots

_Screenshots coming soon._

## Features

- Pitch count tracking: balls, strikes, fouls, and outs — auto-advances on walk, strikeout, or end of inning
- Run scoring for home and away teams, with the batting team highlighted
- Inning tracker with top/bottom half display
- Configurable game clock with expiry alert
- Undo/redo for accidental taps
- Volume button assignments for heads-up pitch counting without looking at the screen
- Multiple saved configurations for different rule sets (rec league, tournament, etc.)
- Named teams with color coding, saved per configuration
- Flexible foul rules: fouls as strikes, strike cap, foul outs, track-only, or off
- Large button layout option for single-hand use
- Share final score via any Android share target

## Screens

- **Clicker** — the main game screen; tap to count pitches and runs, long-press the Clicker tab for end-of-game options
- **Teams** — manage team names and colors for the active configuration
- **Settings** — adjust game rules, configure volume buttons, manage saved configurations

## Configuration System

Settings are grouped into named configurations (e.g., "Default", "Tournament"). Each configuration stores pitch count rules, foul mode, game length, and UI preferences. Switching configurations resets the current game state. The Default configuration cannot be deleted.

## Building from Source

Requires Android Studio. Clone the repo, open the project, and run on a device or emulator running Android 8.0+. No API keys or external services required.

## License

_To be determined._
