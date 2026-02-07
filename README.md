# AE2 Mod ID View Cell

[![Available on CurseForge](https://cf.way2muchnoise.eu/title/1456030.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/ae2-mod-id-view-cell)
[![CurseForge Downloads](https://cf.way2muchnoise.eu/1456030.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/ae2-mod-id-view-cell)

A custom Applied Energistics 2 view cell that filters items by **mod ID**, using a simple GUI with **whitelist** and **blacklist** lists.

## Download
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/ae2-mod-id-view-cell

## Requirements
- Minecraft 1.12.2
- AE2 (UEL)

## Features
- Mod ID filtering using **Whitelist** and **Blacklist**
- Paste multiple ModIDs at once (comma, semicolon, or whitespace separated)
- Automatic normalization (trim, lowercase, optional `@` prefix removal)
- De-duplicated lists while preserving insertion order
- Mouse wheel scrolling with a visible scroll indicator
- Keyboard navigation and shortcuts

## Usage
1. Hold the cell in your hand and **Shift + Right Click** to open the filter GUI.
2. Type one or more ModIDs into the entry field (examples: `appliedenergistics2`, `mekanism`, `thermalfoundation`).
3. Click **Add to Whitelist** or **Add to Blacklist** (or press **Enter** to add to the active list).
4. Click **Save** (or press **Enter** with an empty input) to apply changes.
5. Place the cell into any AE2 terminal View Cell slot (ME Terminal, wireless terminal, etc.).

### Input tips
You can paste multiple entries like:
- `ae2 mekanism thermalfoundation`
- `ae2, mekanism; thermalfoundation`
- `@ae2 @mekanism`

## Controls and shortcuts
- **Tab**: Toggle focus on the entry field
- **Enter**:
  - When the entry field is focused and has text: add to the active list
  - When the entry field is focused and empty: Save
  - When the entry field is not focused: Save
- **Ctrl + S**: Save
- **Delete (Supr)**: Remove selected entry, or if none is selected remove the latest added entry
- **Up / Down**: Move selection in the active list and auto-scroll to keep it visible
- Mouse wheel over a list: Scroll that list (also sets it as the active list)

## Development
This is a Minecraft Forge mod (Minecraft 1.12.2).

Typical workflow:
1. Clone the repository
2. Import into your IDE (IntelliJ IDEA or Eclipse)
3. Run the ForgeGradle setup tasks for your environment
4. Build using Gradle

## Contributing
Issues and pull requests are welcome. If you submit a PR, keep changes focused and include a short description of what changed and why.

## License
MIT (see `LICENSE`).
