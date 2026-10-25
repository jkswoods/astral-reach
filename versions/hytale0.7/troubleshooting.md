---
title: "Troubleshooting"
---

# Troubleshooting {#troubleshooting}

## The mod does not load {#the-mod-does-not-load}

Use a matching development candidate for Hytale 0.7.*, keep only one AstralReach JAR in the mods directory and enable it in the world's Mods tab. Dedicated servers require Java 25. Read [installation](installation.md) and keep the previous world/JAR pair available.

## I cannot find nickel or oil shale {#i-cannot-find-nickel-or-oil-shale}

Enter `/astral-frontier` and explore newly generated frontier rock. Look roughly 5–22 blocks below the local surface. Previously saved chunks are preserved, so veins are not inserted retroactively into old terrain.

## A processor stops {#a-processor-stops}

Check the exact recipe inputs, finite fuel and enough output space for the complete result. Native processors pause when those conditions are not met. Smelting an ingot does not automatically press it into a plate or fill a cell; move materials to the next appropriate machine.

## The launch platform will not form {#the-launch-platform-will-not-form}

Use 25 Launch Pad Modules in five touching rows of five, all at the same height. Each module occupies 5×5 blocks, with roots five blocks apart. Place the separate console outside the midpoint of an edge, 14 blocks from the pad centre. Check its pad direction before choosing a blueprint.

Missing modules, incomplete occupied cells, unloaded areas and obstructions prevent construction. Restore the structure or clear the site. A fully supplied plan that was blocked before starting has a **Retry construction** control.

## I want to change a paid blueprint {#i-want-to-change-a-paid-blueprint}

Choose **Cancel and recover materials** first. Make room for every supplied item. If the full refund cannot fit, the console keeps everything in place. A completed rocket must be drained, idle and removed by its owner before the next construction plan.

## Launch is refused {#launch-is-refused}

The rocket needs eight full cells' worth of propellant, a complete platform, clear launch airspace and a safe place for its owner to stand. The landing site is checked before departure is charged; the new flight prototype also checks an orbital arrival. Follow the flight-control message to clear the obstruction or complete the active journey.

## The map is unavailable or a body is locked {#the-map-is-unavailable-or-a-body-is-locked}

Orbital navigation belongs to the newer flight candidate; see [development status](status.md). Its 3D map opens after reaching space and the booster return. `/astral-map` cannot open it from the launch pad. Lune is the current selectable landing destination. Other bodies describe the system's intended resource gates while their routes remain under development.

## I cannot return {#i-cannot-return}

Use the Lune beacon or `/astral-return` for the same paid reserve. The new flight prototype also offers a home-return control in its map. They do not create a new journey. Keep your saved departure point clear: an obstructed home landing holds the reserve until it can be used safely.

If your saved home world is unavailable, preserve its files and restore the matching backup. Do not delete journey or world data to work around a development problem.

## A visual, sound or control feels wrong {#a-visual-sound-or-control-feels-wrong}

Record the Hytale version, exact AstralReach candidate and the action that caused it. Include a screenshot where useful, keeping private account details out of it. Report problems through [GitHub issues](https://github.com/jkswoods/astral-reach/issues). Human visual, listening and comfort feedback is still part of development.
