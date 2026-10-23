# Configuration and access

Enable AstralReach separately for the world where you want to use it. A new flat test world is useful for the large launch site; the persistent frontier supplies the resource chain.

## Player commands

| Command | Purpose |
| --- | --- |
| `/astral-frontier` | Enter or load the persistent Orbis resource frontier; grants no inventory |
| `/astral-guide` | Show the resource and fuel route in chat |
| `/astral-map` | Flight prototype: reopen orbital navigation after reaching space on a paid journey |
| `/astral-return` | Recover the existing paid Lune return; the flight prototype also supports orbit; grants no new fuel or free voyage |

The frontier command cannot bypass an active paid journey. Finish the journey before entering a different frontier session.

## Ownership

The launch console belongs to its placing player. That player supplies the blueprint and operates its rocket. Other players cannot operate the console or remove its protected pad modules. Rocket removal requires an idle mission and an empty tank.

The native server controls the material quantities, fuel exchanges, construction progress and paid return. Client displays are controls and status views, rather than a separate inventory store.

## Development support

The current candidate has been exercised locally with one client. **Real multiplayer acceptance remains pending.** Do not infer production-server compatibility from the ownership rules or screenshots. There are currently no documented player settings for fuel costs, additional destinations or flight timing.

Keep the whole save and matching JAR as a backup. Preserve the saved home world when a journey is active: return uses its actual identity and saved departure position.
