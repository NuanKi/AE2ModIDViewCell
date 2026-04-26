# Features

The Mod ID View Cell filters AE2 terminal entries by the mod ID that owns each entry. For example, blacklisting `ae2` hides Applied Energistics 2 entries, while whitelisting `minecraft` shows only vanilla Minecraft entries.

## Filtering

- Whitelist and Blacklist lists are edited separately.
- Entries are normalized when saved: spaces are trimmed, uppercase letters become lowercase, duplicate entries are removed, and an optional leading `@` is ignored.
- Entries are regular expressions matched against the full mod ID.
- If a whitelist has entries, only matching mod IDs are shown.
- Blacklist entries hide matching mod IDs.
- Invalid regular expressions are ignored and logged.

## Examples

- `ae2` matches only the exact mod ID `ae2`.
- `ae.*` matches `ae2`.
- `mekanism` matches only `mekanism`.
- `meka.*` matches `mekanism`, `mekanismgenerators`, and other mod IDs that start with `meka`.
- `.*storage.*` matches any mod ID containing `storage`.
- `@ae2` is saved as `ae2`.

## GUI

- Paste multiple mod IDs at once using commas, semicolons, spaces, or new lines.
- Tab completion suggests installed mod IDs.
- Whitelist and Blacklist entries can be selected, removed, cleared, or moved between lists.
- Multi-selection works inside one list at a time.
- The guide button opens the in-game AE2 guide page for the Mod ID View Cell.

## Usage

1. Hold the cell in your main hand and right-click to open the filter GUI.
2. Type one or more mod IDs into the entry field.
3. Click Add to Whitelist or Add to Blacklist.
4. Click Save, press Enter with an empty input, or press Ctrl+S.
5. Place the cell into an AE2 terminal View Cell slot.

The cell can also be edited from AE2 terminal View Cell slots. It does not open from normal player inventory slots. Configured view cells are prioritized into View Cell slots when shift-left-clicked into a terminal; if those slots are full, normal AE2 storage insertion can still happen. Shift-right-click a view cell from the player inventory while a terminal is open to send it directly to the ME network instead.

## Controls

- Tab focuses the entry field when it is not focused.
- Tab cycles forward through completion matches when the entry field is focused.
- Tab unfocuses the entry field when there is no current mod ID token or completion match.
- Shift+Tab cycles backward through completion matches.
- Enter adds the typed text to the active list when the entry field has text.
- Enter saves when the entry field is empty.
- Ctrl+S saves.
- Delete or Backspace removes selected entries.
- Delete or Backspace removes the latest added entry when nothing is selected and the entry field is not focused.
- Up and Down move selection inside the active list.
- Mouse wheel scrolls the list under the cursor and makes it active.
- Ctrl+Click toggles individual entries in the active list.
- Shift+Click selects a range in the active list.
- Move buttons transfer selected entries between Whitelist and Blacklist.
