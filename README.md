[![Downloads](http://cf.way2muchnoise.eu/1456030.svg)](https://www.curseforge.com/minecraft/mc-mods/ae2-mod-id-view-cell)
[![MCVersion](http://cf.way2muchnoise.eu/versions/1456030.svg)](https://www.curseforge.com/minecraft/mc-mods/ae2-mod-id-view-cell)
[![GitHub issues](https://img.shields.io/github/issues/NuanKi/AE2ModIDViewCell.svg)](https://github.com/NuanKi/AE2ModIDViewCell/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/NuanKi/AE2ModIDViewCell.svg)](https://github.com/NuanKi/AE2ModIDViewCell/pulls)

# AE2 Mod ID View Cell

A custom Applied Energistics 2 view cell that filters items by **mod ID**, using a simple GUI with **whitelist** and **blacklist** lists.

- See full release history in [`CHANGELOG.md`](CHANGELOG.md)

## CurseForge
https://www.curseforge.com/minecraft/mc-mods/ae2-mod-id-view-cell

## Features
- Mod ID filtering using **Whitelist** and **Blacklist**
- Paste multiple ModIDs at once (comma, semicolon, or whitespace separated)
- Automatic normalization (trim, lowercase, optional `@` prefix removal)
- De-duplicated lists while preserving insertion order
- Mouse wheel scrolling with a visible scroll indicator
- Keyboard navigation and shortcuts
- **Tab completion** for ModIDs (based on installed mods)
- **Multi-selection** (Ctrl+Click and Shift+Click) for fast bulk actions
- **Transfer buttons** to move entries between Whitelist and Blacklist

## Usage
1. Hold the cell in your hand and **Shift + Right Click** to open the filter GUI.
2. Type one or more ModIDs into the entry field (examples: `appliedenergistics2`, `mekanism`, `thermalfoundation`).
3. Click **Add to Whitelist** or **Add to Blacklist** (or press **Enter** to add to the active list).
4. Apply changes by clicking **Save**, pressing **Enter** with an empty input, or pressing **Ctrl + S**.
5. Place the cell into any AE2 terminal View Cell slot (ME Terminal, wireless terminal, etc.).

### Input tips
You can paste multiple entries like:
- `ae2 mekanism thermalfoundation`
- `ae2, mekanism; thermalfoundation`
- `@ae2 @mekanism`

## Controls and shortcuts
- **Tab**:
    - If the entry field is not focused: focus it
    - If the entry field is focused: tab-complete the current token and **cycle forward** through matches
- **Shift + Tab**: cycle **backwards** through matches
- **Enter**:
    - When the entry field is focused and has text: add to the active list
    - When the entry field is focused and empty: Save
    - When the entry field is not focused: Save
- **Ctrl + S**: Save
- **Delete (Supr)**:
    - Remove selected entry/entries
    - If nothing is selected: remove the latest added entry
- **Up / Down**: Move selection in the active list and auto-scroll to keep it visible
- Mouse wheel over a list: Scroll that list (also sets it as the active list)
- **Ctrl + Click**: Multi-select individual entries in the active list
- **Shift + Click**: Select a range (from anchor to clicked entry)
- **Move buttons**: Transfer selected entry/entries between Whitelist and Blacklist

## Building
This is a Minecraft Forge mod for 1.12.2.

Typical workflow:
1. Clone the repository
2. Import into your IDE (IntelliJ IDEA or Eclipse)
3. Run the ForgeGradle setup tasks for your environment
4. Build using Gradle

## Contributing
Issues and pull requests are welcome. If you submit a PR, keep changes focused and include a short description of what changed and why.

## License
MIT (see `LICENSE`).