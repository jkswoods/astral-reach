---
title: "Development status"
---

# Development status {#development-status}

**Early development — some features may be incomplete or may not work as expected.** Choose a file from the release list and read its channel and notes. Alpha files may require enabling alpha versions in your launcher.

Astral Reach is a **development project for Hytale 0.7**. This guide describes the support branch and its known limits. Available files and their release notes are listed separately on [GitHub releases](https://github.com/jkswoods/astral-reach/releases).

## Established playable slice {#established-playable-slice}

Local Hytale 0.7.4 gameplay checks cover representative ore gathering, native processing and crafting, finite propellant exchanges, a paid Lune flight and return, and the first Thermal Shield. The large launch-platform candidate also has native checks for its joined deck, both ghost blueprints, paid rocket construction, partial-material recovery, normal saved work and completion fireworks.

The ships shown in the gallery retain their actual large dimensions. Current collision uses a compact control anchor with a decorative exterior. There is no enterable interior or aerodynamic freeflight.

## New flight candidate {#new-flight-candidate}

Local Hytale 0.7.4 flights now show sustained third-person ascent, ignition smoke, engine fire, a fading atmosphere, stars and a bright sun. Kestrel separates its upper ship in space and returns its booster to the launch pad before orbital navigation. A later test extended that sequence to 90 seconds.

The revised interface uses a top-down system chart with clickable planets and route rows. There are no drag, pan, zoom or separate 3D controls. Lune remains reachable; later landing routes remain locked. Camera and passenger presentation still need polish.

The earlier ignition crash has been repaired. New native tests have completed an upper-stage descent and touchdown on Lune, disembarkation, and the paid upper-stage ascent and home approach. The normal saved home state cleared the journey and spent its protected return reserve once. Broad crater basins, rolling regolith and the four revised raised mineral deposits have also been inspected in Hytale.

**Passenger presentation remains under repair.** Players reported the character body lagging behind the ship, jitter on foot on Lune and weak-looking landing-leg connections. A duplicate ship above the landing site was reproduced; the repair passed a normal lunar reload with one ship. The latest candidate adds native acknowledged player movement, closer lunar landing sites and stronger hull-mounted landing gear. Broader flight, on-foot movement and final visual checks remain open. Keep each candidate with its matching backup.

The candidate compiles against pinned 0.7.3 APIs and passes isolated asset, recipe, terrain and saved-state checks on 0.7.3 and 0.7.4. Those checks establish integration; they do not replace a complete in-game flight or human visual and listening acceptance.

Gallery captions identify the earlier launch-platform photographs and the newer native orbital view separately.

## Open limits {#open-limits}

- A full ordinary survival walkthrough from raw starting resources remains unverified; some local functional checks used staged stock materials or equipment.
- Human visual, sound-level, comfort and real multiplayer acceptance remain pending.
- The declared Hytale 0.7 range starts at 0.7.3; it does not mean every later patch has been tested.
- Lune is the implemented destination. Later-world landing routes, interiors and freeflight remain development work. Oxygen canisters and diffuser fields have representative local tests; underwater, exhaustion and real multiplayer checks remain open.

Keep complete backups when trying a supplied candidate. Use [GitHub issues](https://github.com/jkswoods/astral-reach/issues) for feedback with the exact game and mod build, and avoid including private account information.


## Revised native controls {#revised-native-controls}

The revised interface adds a native destination list and clickable system chart, selected resource and equipment details, clear protected fuel, simpler construction and flight panels, per-canister diffuser charge bars and compact oxygen/flight displays. Later destinations remain locked. The separate 3D option has been removed; the top-down chart now fills its panel.

Representative native Hytale0.7.4 checks cover route-list and chart selection, disabled future routes, paid Lune travel and return, diffuser controls with partially used oxygen, paid service-part construction, flight-control access and normal saved reload. The revised interface also renders clean item icons without grey backplates, native tooltips, inset canister meters and the repaired Kestrel blueprint. A fresh test cancelled a real paid launch with the large red Abort button, returned all 9,000 fuel units and retained the full tank after a normal quit and reload. That check used staged parts, fuel and equipment.

The latest native captures show stronger cyan selection, blue primary actions, deeper crater detail and a tighter blueprint summary. Both rocket illustrations and the missing-part construction panel were checked at playing scale and in matching enlarged views. The guide shows actual gameplay captures. Human appearance, comfort, listening and real multiplayer acceptance remain separate checks.

The chart’s asteroid belt sits closer to the Sun, inside Orbis’s orbit. The landing note is inset from its frame. Press Esc to hide Destinations; the on-screen hint and dismissal reminder explain `/astral-map` to reopen it without spending fuel. These controls were checked in native Hytale 0.7.4.


## Rocket catalogue and construction {#rocket-catalogue-and-construction}

The development branch adds a five-card catalogue with original rocket thumbnails, measured dimensions and full component costs. Picking a blueprint opens its construction screen; saved selection makes that the default when the console reopens. Required, supplied and carried quantities remain server controlled. Change rocket refunds paid components before returning to selection. Returned assemblies retain their original blueprint and credit.

Saturn V, Soyuz and Space Shuttle have original models, icons, ghosts, construction recipes and flight-stage assets. Representative native checks cover the catalogue, partial supply and exact refund, saved selection, complete Soyuz construction, and placement/Use on all three new items. Full new-vehicle flights, all orientation/collision cases, final human appearance, listening and real multiplayer acceptance remain open. Future range and progression differences are deliberately deferred.

## Rescue and expedition preview {#rescue-and-expedition-preview}

Rescue Beacons use finite Beacon Cell power and share signals only with invited players. A Field Receiver provides direction, distance and available oxygen information. Lunar survey outposts contain recoverable records and salvage. Follow [rescue and lunar expeditions](rescue-and-expeditions.md) for equipment and progression.

Lost Survey awards a personal, non-stacking 5% fuel benefit. Life Support Survey unlocks the forty-minute oxygen tank recipe; Surface Mobility Survey unlocks the two-seat electric rover recipe. Both equipment rewards require two records from one matching site and normal paid crafting. Local Hytale 0.7.5 checks cover those unlocks, paid crafting, a full tank fill, normal saved progress, rover packing/redeployment and powered lunar travel with F exit.

The new landscape mixes crater plains, rocky ridges, lava-tube survey routes and small mineable impact fragments. New terrain and outposts appear only in unexplored chunks; existing builds and explored terrain remain intact.

Rover camera framing remains rough, and Space braking is unverified. Allow room to coast to a stop. Real passenger/rescuer behavior, sound quality and a complete ordinary survival expedition still need testing. Flight Control shows exact fuel units; the partial fuel-cell inventory bar may be unclear.

## Orbital stations {#orbital-stations}

One personal station per player provides a protected linear concourse, nine external docking airlocks and small starter platforms beside open construction space. Solar arrays, batteries and canister-fed life support support station building. Tracking equipment and moving sky lights make stations visible from Orbis. See [orbital stations](stations.md).

Real second-client visiting, invitation revocation, every rocket/berth combination and some interrupted undocking cases remain unverified. Telescope zoom and identification also need further testing. Keep a funded route home and carry spare oxygen.
