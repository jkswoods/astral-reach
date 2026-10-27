## Early development

**Early development — some features may be incomplete or may not work as expected.** This experimental build uses the Release channel so it is visible in the CurseForge client. Keep a world and player-data backup when upgrading.

- Redesigned native destination selection, construction, flight-control and oxygen interfaces, with clearer resources, equipment, charge levels and protected return fuel.
- Simple clickable planets and route rows. The asteroid belt sits closer to the Sun; landing information has a clearer inset. Press Esc to hide Destinations and use `/astral-map` to reopen it while in orbit.
- Large red **ABORT LAUNCH** cancels an active departure. A representative paid cancellation restored the full reserved fuel and retained it after normal save/reload.
- Original home-world nickel and oil shale, native fueled processing, finite propellant, full-size Odyssey/Kestrel rockets, joined launch-platform construction and the paid Lune route.

Hytale support: `>=0.7.3 <0.8.0`. Built against pinned 0.7.3 APIs; representative native gameplay checks used 0.7.4. Requires Java 25. The range does not certify every future patch.

**Current limits:** passenger movement, camera and landing presentation may be rough. Full raw-resource survival progression, listening quality and real multiplayer verification remain open. Lune is the playable destination; later landing routes are locked. Interiors and aerodynamic freeflight are not included. Read the [player guide and development status](https://jkswoods.github.io/astral-reach/hytale0.7/status/).

With Hytale stopped, install this release’s JAR in `UserData/Mods`, remove older Astral Reach JARs and enable AstralReach in the world’s Mods settings. No separate resource ZIP is needed.
