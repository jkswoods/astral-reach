# Installation

Astral Reach targets **Hytale 0.7.\***. The guide describes the **0.2.0-dev development candidate**. It compiles against the official 0.7.3 server APIs; local gameplay checks use the 0.7.4 client. Other patches in the 0.7 family have not all been tested. A dedicated server needs Java 25.

There is currently **no public playable JAR download in this repository**. It publishes the player guide and screenshots while the mod is in development. Use a candidate supplied by the project only, and keep its exact version when reporting a problem.

## Try a development candidate

1. Back up the complete world and player data before changing builds.
2. Close the game and its local server.
3. Put one AstralReach JAR in Hytale's `UserData/Mods` directory. Keep older copies outside that directory so they do not load together.
4. Create a **separate test world** and enable AstralReach in that world's **Mods** tab. A flat world makes planning a launch site easier.
5. Enter the world and use `/astral-frontier` to visit the resource frontier. The command grants no items or fuel.

The frontier and Lune are persistent worlds within the save. Existing generated chunks are preserved; new nickel and oil-shale veins appear in newly generated frontier rock.

## Update or remove

Keep the world, player data and matching previous JAR together as a backup. Complete an expedition and recover supplied construction materials before changing versions where possible. Stop the server before replacing a JAR.

Astral blocks, machines, paid journeys and inventories belong to the mod. Removing it from a world containing those things can make them unavailable. Restore the matching backup to recover an earlier build. A fresh world is the safest place to evaluate a development candidate.

Continue with [your first expedition](getting-started.md). See [development status](status.md) for the current limits.
