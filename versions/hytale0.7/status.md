---
title: "Development status"
---

# Development status {#development-status}

Astral Reach is a **development project for Hytale 0.7**. This guide describes the support branch and its known limits. Available files and their release notes are listed separately on [GitHub releases](https://github.com/jkswoods/astral-reach/releases).

## Established playable slice {#established-playable-slice}

Local Hytale 0.7.4 gameplay checks cover representative ore gathering, native processing and crafting, finite propellant exchanges, a paid Lune flight and return, and the first Thermal Shield. The large launch-platform candidate also has native checks for its joined deck, both ghost blueprints, paid rocket construction, partial-material recovery, normal saved work and completion fireworks.

The ships shown in the gallery retain their actual large dimensions. Current collision uses a compact control anchor with a decorative exterior. There is no enterable interior or aerodynamic freeflight.

## New flight candidate {#new-flight-candidate}

Local Hytale 0.7.4 flights now show sustained third-person ascent, ignition smoke, engine fire, a fading atmosphere, stars and a bright sun. Kestrel separates its upper ship in space and returns its booster to the launch pad before orbital navigation. A later test extended that sequence to 90 seconds.

The native orbital view now contains three-dimensional bodies with rotation, tilt, zoom, pan and resource-route selection. Lune remains reachable; later landing routes remain locked. Camera and passenger presentation still need polish.

The earlier ignition crash has been repaired. New native tests have completed an upper-stage descent and touchdown on Lune, disembarkation, and the paid upper-stage ascent and home approach. The normal saved home state cleared the journey and spent its protected return reserve once. Broad crater basins, rolling regolith and the four revised raised mineral deposits have also been inspected in Hytale.

**Passenger presentation remains under repair.** Players reported the character body lagging behind the ship, jitter on foot on Lune and weak-looking landing-leg connections. A duplicate ship above the landing site was reproduced; the repair passed a normal lunar reload with one ship. The latest candidate adds native acknowledged player movement, closer lunar landing sites and stronger hull-mounted landing gear. Its full flight, on-foot movement and final visual checks are still pending. Keep each candidate with its matching backup.

The candidate compiles against pinned 0.7.3 APIs and passes isolated asset, recipe, terrain and saved-state checks on 0.7.3 and 0.7.4. Those checks establish integration; they do not replace a complete in-game flight or human visual and listening acceptance.

Gallery captions identify the earlier launch-platform photographs and the newer native orbital view separately.

## Open limits {#open-limits}

- A full ordinary survival walkthrough from raw starting resources remains unverified; some local functional checks used staged stock materials or equipment.
- Human visual, sound-level, comfort and real multiplayer acceptance remain pending.
- The declared Hytale 0.7 range starts at 0.7.3; it does not mean every later patch has been tested.
- Lune is the implemented destination. Later-world landing routes, interiors and freeflight remain development work. Oxygen canisters and diffuser fields have representative local tests; underwater, exhaustion and real multiplayer checks remain open.

Keep complete backups when trying a supplied candidate. Use [GitHub issues](https://github.com/jkswoods/astral-reach/issues) for feedback with the exact game and mod build, and avoid including private account information.


## Revised native controls {#revised-native-controls}

A private interface candidate adds a native destination list and clickable system chart, selected resource and equipment details, clear protected fuel, simpler construction and flight panels, per-canister diffuser charge bars and compact oxygen/flight displays. Later destinations remain locked. The existing 3D chart stays available as a separate exploration view.

Representative native Hytale 0.7.4 checks now cover route-list and chart selection, disabled future routes, the separate 3D view and return, paid Lune travel, diffuser controls and partial oxygen, paid service-part construction, flight-control access and normal saved reload. Close comparison with the selected design found visual differences in typography, frames, spacing and planet relief. The latest in-game review still found heavy headings and bevels, flat-looking planet relief, an unclear rocket preview and canister meters outside their frames. Those details are being revised; the corrected appearance needs a fresh native check. Generated designs and source previews are not gameplay evidence. Human appearance, comfort and listening approval remain separate checks.
