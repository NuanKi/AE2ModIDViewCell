# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.5]

### Added
- ModID entry field tab completion for installed mods (Tab cycles matches, Shift+Tab cycles backwards).
- Multi-selection in Whitelist and Blacklist using Ctrl+Click, plus Shift+Click range selection.
- Bulk transfer support, transfer buttons move the current multi-selection between lists.
- Mutually exclusive selection between lists, selecting entries in Whitelist clears any selection in Blacklist (and vice versa), so you cannot multi-select across both lists at the same time.

### Changed
- Delete key removal now applies to the current multi-selection when present.
- `GuiModViewCell` code organization improved for readability (no functional changes).

## [1.1.2]

### Added
- Chinese (Simplified) translation (community contribution by @ZHAY10086, PR #1).

## [1.1.0]

### Added
- Transfer buttons between filter lists:
    - Move selected entry Whitelist to Blacklist.
    - Move selected entry Blacklist to Whitelist.
- Transfer buttons enable or disable automatically based on whether the source list has a selection.
- After transferring, the moved item becomes selected in the target list and the list scrolls to keep it visible.

### Fixed
- Improved selection and scroll clamping after list mutations (transfer, remove, add) to prevent invalid indices and keep scroll positions in range.

## [1.0.7]

### Added
- Scroll indicators for both Whitelist and Blacklist list boxes (track + thumb), only shown when the list can scroll.
- Keyboard shortcuts:
    - Tab toggles focus on the ModID entry field.
    - Enter while the entry field is focused and has text adds to the active list (Whitelist or Blacklist).
    - Enter while the entry field is focused and empty saves.
    - Enter while the entry field is not focused saves.
    - Ctrl+S saves.
    - Delete removes the selected entry, or if none is selected removes the latest added entry.
    - Up/Down moves selection in the active list and auto-scrolls to keep it visible.
- Active list tracking so Enter, Up/Down, and Delete apply consistently based on the last interacted list (clicking or scrolling a list, or pressing its add button).

### Changed
- List text trimming now uses Minecraft's built-in font trimming helper for better performance and consistent results.

## [1.0.5]

### Fixed
- Fixed mixins not applying on obfuscated clients by targeting both deobf and SRG method names for:
    - `SlotRestrictedInput.isItemValid` (`func_75214_a`)
    - `ContainerMEMonitorable.transferStackInSlot` (`func_82846_b`)

## [1.0.0]

### Added
- Initial publish.