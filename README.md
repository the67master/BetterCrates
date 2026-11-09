# BetterCrates 1.3.1

Customizable crates and KeyAll for **Spigot 1.12.2 / Java 8**.

## Features

- Create and configure crates and keys in-game.
- Reward editor with automatic chance balancing and manual percentages.
- Manual reward chances cannot exceed 100%; remaining chance is shared by automatic rewards.
- Configurable crate blocks, key items, holograms, opening messages and GUI filler.
- KeyAll timer with random, all-selected-keys and weighted-key distribution.
- Optional PlaceholderAPI placeholder: `%bckey%` shows the remaining KeyAll time.
- English and Spanish language options.

## Commands

- `/bc` or `/bc gui` — open the admin menu.
- `/bc create <id>` — create a crate (a matching key is created if needed).
- `/bc givekey <player> <id> [amount]` — give keys.
- `/bc givecrate <player> <id> [amount]` — give crate blocks.
- `/bc list` — list crates.
- `/bc reload` — reload configuration.
- `/bc keyall set <duration>` — set the recurring KeyAll interval, e.g. `1d6h`, `1h5m`, `5m12s`.
- `/bc keyall mode <random|all|weighted>` — choose how keys are distributed.
- `/bc keyall add <key-id> [weight]` — add a key to the KeyAll pool. Weights are relative; for example, weights 60 and 40 produce 60% and 40%.
- `/bc keyall remove <key-id>` — remove a key from the pool.
- `/bc keyall list` — show each key's chance in random/weighted mode, or guaranteed status in all mode.
- `/bc keyall time` — show the remaining time.

## Configuration

- `settings.language`: `en` or `es`.
- `settings.prefix`: plugin chat prefix.
- `customization.gui`: filler material, legacy data value and name.
- `customization.crate`: default block, display name and glint.
- `customization.key`: default material, display name, glint and lore.
- `customization.hologram`: default enabled state and three default lines.
- `customization.messages`: default private, global and reward messages for new crates.
- `keyall.interval-seconds`: recurring event interval in seconds.
- `keyall.distribution`: `random`, `all` or `weighted`.
- `keyall.keys`: selected key IDs and their relative weights. An empty section uses every registered key.
- `keyall.broadcast`: global KeyAll announcement.

Install PlaceholderAPI alongside BetterCrates to use `%bckey%` in supported placeholders.
