---
navigation:
    parent: ae2:items-blocks-machines/items-blocks-machines-index.md
    title: Mod ID View Cell
    icon: ae2modidviewcell:mod_id_view_cell
    position: 410
categories:
    - tools
item_ids:
    - ae2modidviewcell:mod_id_view_cell
---

# Mod ID View Cell

<ItemImage id="ae2modidviewcell:mod_id_view_cell" scale="2" />

The Mod ID View Cell is a variant of the <ItemLink id="ae2:view_cell" /> that filters terminal entries by the mod that owns them.

For example, <ItemLink id="ae2:certus_quartz_crystal" /> has the mod ID `ae2`.
Adding `ae2` to the blacklist hides AE2 entries.
Adding `minecraft` to the whitelist only allows vanilla Minecraft entries.

## Opening The Filter Screen

Right-click the cell in your main hand, or right-click it while hovering over it in an AE2 terminal View Cell slot, to open the filter screen.
The filter screen does not open from normal player inventory slots.

The top field accepts one or more mod IDs separated by spaces or commas.
After typing, use **Add to Whitelist** or **Add to Blacklist**.

## Terminal Transfer

When a configured view cell is shift-left-clicked from your inventory into an AE2 terminal, it tries to enter an empty View Cell slot first.
If you want to store the view cell in the ME network instead, shift-right-click it from your inventory while the terminal is open.

## Screen Controls

The filter screen is split into an entry field, two Add buttons, and two lists.

While typing in the entry field:

- Separate multiple entries with spaces or commas.
- Press `Tab` to complete the current mod ID from loaded mods.
- Press `Tab` with no current completion to leave the entry field.
- Press `Shift + Tab` to cycle completions backwards.
- Press `Enter` to add the typed entries to the active list.
- Press `Ctrl + S` to save the cell.

When the entry field is not focused:

- Press `Tab` to return to the entry field.
- Press `Ctrl + S` to save the cell.
- Press `Enter` to save the cell.
- Press `Delete`, `Supr`, or `Backspace` to remove the latest added entry when no entry is selected.

When a list entry is selected:

- Press `Up` or `Down` to move through entries in the selected list.
- Press `Left` or `Right` to switch between the whitelist and blacklist lists.
- Press `Shift + Up` or `Shift + Down` to select a range in the same list.
- Press `Ctrl + Up` or `Ctrl + Down` to add entries to the current selection while moving.
- Press `Delete`, `Supr`, or `Backspace` to remove selected entries.

Mouse selection also supports multiple entries:

- Click an entry to select it.
- `Shift + Click` selects a range in the same list.
- `Ctrl + Click` toggles one entry in the same list.
- Selecting the other list clears the previous list selection, so selections never span whitelist and blacklist at the same time.
- Scroll over a list to move through its entries.

## Whitelist And Blacklist

The screen has two entry lists:

- **Whitelist**: only matching mod IDs are allowed through.
- **Blacklist**: matching mod IDs are hidden.

If the whitelist is empty, it does not restrict the terminal by itself.
If the blacklist is empty, it does not hide anything by itself.
If both lists are empty, the cell behaves like an unconfigured view cell.

Select entries in either list to manage them:

- Use `>` and `<` to move selected entries between whitelist and blacklist.
- Use **Remove Selected** to delete selected entries.
- Use **Clear All** to empty both lists.
- Use **Save** or `Ctrl + S` to apply the filters.
- Use **Cancel** to close without saving.

## Examples

Use exact mod IDs for normal filtering:

- `ae2` matches Applied Energistics 2.
- `minecraft` matches vanilla Minecraft.
- `mekanism` matches Mekanism.
- `thermal` matches Thermal Series entries whose mod ID is exactly `thermal`.

Advanced users can use full-match regex patterns.
This means the whole mod ID must match the pattern:

- `ae.*` matches `ae2`.
- `meka.*` matches `mekanism` and `mekanismgenerators`.
- `thermal.*` matches `thermal`, `thermalfoundation`, or similar Thermal addon IDs.
- `.*storage.*` matches mod IDs that contain `storage`.

Invalid regex entries are ignored.

Unlike the standard view cell, this cell is configured from its own screen.
It is not edited in the Cell Workbench because fuzzy and inverter cards are not used for mod ID filtering.

## Recipe

<Recipe id="ae2modidviewcell:mod_id_view_cell" />
