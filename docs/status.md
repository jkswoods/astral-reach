# Development status

Astral Reach is a **0.2.0-dev development project for Hytale 0.7.\***. This public repository distributes the player guide and curated media. It currently contains no playable JAR download and is not a production mod release.

## Established playable slice

Local Hytale 0.7.4 gameplay checks cover representative ore gathering, native processing and crafting, finite propellant exchanges, a paid Lune flight and return, and the first Thermal Shield. The large launch-platform candidate also has native checks for its joined deck, both ghost blueprints, paid rocket construction, partial-material recovery, normal saved work and completion fireworks.

The ships shown in the gallery retain their actual large dimensions. Current collision uses a compact control anchor with a decorative exterior. There is no enterable interior or aerodynamic freeflight.

## New flight candidate

The next local candidate implements a sustained third-person launch, ignition smoke, attached engine fire, a fading atmosphere, a star-filled orbital view with a bright sun, and space-only orbital navigation. Kestrel's sequence separates its upper ship and shows a booster flip and home-pad return before navigation.

The prototype compiles against the pinned 0.7.3 APIs and has isolated asset and saved-state checks. **Native 0.7.4 testing currently finds a client crash at ignition.** The tested disconnect recovery restored the full 8,000-unit reservation. The cause is being isolated; the longer ascent, camera and orbital controls have not passed the complete native flight.

The current photographs show the earlier locally tested launch-platform build, `0.2.0-dev` with artifact hash beginning `5f722a35`. It uses a short departure to Lune. The new prototype is not presented as a completed native acceptance result.

## Open limits

- A full ordinary survival walkthrough from raw starting resources remains unverified; some local functional checks used staged stock materials or equipment.
- Human visual, sound-level, comfort and real multiplayer acceptance remain pending.
- The 0.7.* compatibility declaration does not mean every patch has been tested.
- Lune is the implemented destination. Later worlds, their upgrade recipes, oxygen systems, interiors and freeflight remain development work.

Keep complete backups when trying a supplied candidate. Use [GitHub issues](https://github.com/jkswoods/astral-reach/issues) for feedback with the exact game and mod build, and avoid including private account information.
