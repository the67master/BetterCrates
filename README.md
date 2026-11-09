# BetterCrates

A customizable crates and KeyAll plugin for **Spigot 1.12.2 / Java 8**.

## Main features

- In-game crate and key creation and configuration menus.
- Reward editor with automatic chance balancing and manual chance overrides.
- Configurable crate blocks, key items, holograms and opening messages.
- KeyAll timer with random, all-keys and weighted distribution modes.
- Optional PlaceholderAPI integration: `%bckey%` displays the remaining KeyAll time.
- English by default, with English (`en`) and Spanish (`es`) message/GUI text options.

## Commands

- `/bc` or `/bc gui` — open the admin menu.
- `/bc create <id>` — create a crate.
- `/bc givekey <player> <id> [amount]` — give a key.
- `/bc givecrate <player> <id> [amount]` — give a crate block.
- `/bc list` — list crates.
- `/bc reload` — reload configuration.
- `/bc keyall set <duration>` — set the recurring KeyAll interval, e.g. `1d6h`, `1h5m`, `5m12s`.
- `/bc keyall mode <random|all|weighted>` — choose the distribution mode.
- `/bc keyall add <key-id> [weight]` — add a key to the KeyAll pool (default weight: 1).
- `/bc keyall remove <key-id>` — remove a key from the pool.
- `/bc keyall list` — show the configured KeyAll pool.
- `/bc keyall time` — show the remaining time.

## Configuration

- `settings.language`: `en` or `es`.
- `customization.gui`: filler material, data value and name.
- `customization.crate`: default crate block and glint.
- `customization.key`: default key material and glint.
- `customization.hologram`: default enabled state and lines.
- `keyall.interval-seconds`: recurring event interval in seconds.
- `keyall.distribution`: `random`, `all` or `weighted`.
- `keyall.keys`: selected key IDs and their weights. An empty section makes all registered keys eligible.
- `keyall.broadcast`: global KeyAll announcement.

For PlaceholderAPI, install PlaceholderAPI alongside BetterCrates and use `%bckey%` in supported placeholders.
