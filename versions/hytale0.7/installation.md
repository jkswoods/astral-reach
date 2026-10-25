---
title: "Installation"
---

# Installation {#installation}

This guide follows the **Hytale 0.7 support line**. Published files can lag behind branch changes; read the chosen file’s release notes for the features it includes.

This guide covers Astral Reach development on the **Hytale 0.7** support line. It compiles against the official 0.7.3 server APIs; local gameplay checks use the 0.7.4 client. Other patches in the 0.7 family have not all been tested. A dedicated server needs Java 25.

Check [GitHub releases](https://github.com/jkswoods/astral-reach/releases) for files marked compatible with your Hytale game version. If no matching file is available, use the support branch’s build instructions or wait for a compatible release. Keep the identity of the file you actually installed when reporting a problem.

## Try a development candidate {#try-a-development-candidate}

1. Back up the complete world and player data before changing builds.
2. Close the game and its local server.
3. Put one AstralReach JAR in Hytale's `UserData/Mods` directory. Keep older copies outside that directory so they do not load together.
4. Create a **separate test world** and enable AstralReach in that world's **Mods** tab. A flat world makes planning a launch site easier.
5. Enter the world and use `/astral-frontier` to visit the resource frontier. The command grants no items or fuel.

The frontier and Lune are persistent worlds within the save. Existing generated chunks are preserved; new nickel and oil-shale veins appear in newly generated frontier rock.

## Update or remove {#update-or-remove}

Keep the world, player data and matching previous JAR together as a backup. Complete an expedition and recover supplied construction materials before changing versions where possible. Stop the server before replacing a JAR.

Astral blocks, machines, paid journeys and inventories belong to the mod. Removing it from a world containing those things can make them unavailable. Restore the matching backup to recover an earlier build. A fresh world is the safest place to evaluate a development candidate.

Continue with [your first expedition](getting-started.md). See [development status](status.md) for the current limits.
