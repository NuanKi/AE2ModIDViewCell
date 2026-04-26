# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-04-26

### Added

- Initial 1.21.1 rewrite of AE2 Mod ID View Cell for NeoForge and AE2 19.x.
- Mod ID View Cell item that filters AE2 terminal contents by mod ID.
- Whitelist and Blacklist filtering with full-mod-ID regular expression matching.
- Modern GUI for editing filter lists, including paste splitting, normalization, de-duplication, scrolling, list transfer buttons, and multi-selection.
- Tab completion for installed mod IDs.
- Shift tooltip showing the saved Whitelist and Blacklist entries.
- AE2 GuideME page and in-GUI guide button.
- AE2 terminal integration for View Cell slots, including configured View Cell shift-click priority.
- Shift-right-click bypass for sending view cells from player inventory directly into the ME network.
- Save support when editing the cell from the main hand or AE2 terminal View Cell slots.

### Changed

- Rebuilt the old 1.12.2 behavior around modern AE2 APIs, NeoForge networking, item data components, and current screen widgets.
- Kept the mod ID as `midviewcell` for compatibility with the existing project identity.

[Unreleased]: https://github.com/NuanKi/AE2ModIDViewCell/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/NuanKi/AE2ModIDViewCell/tree/v1.0.0
