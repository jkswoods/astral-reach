# Astral Reach

Gather resources, build full-size rockets and use finite fuel for a paid expedition to Lune.

**[Read the player guide](https://jkswoods.github.io/astral-reach/)** · [Rockets and scale](https://jkswoods.github.io/astral-reach/hytale0.7/rockets/) · [Launch platform](https://jkswoods.github.io/astral-reach/hytale0.7/launch-platform/) · [Gallery](https://jkswoods.github.io/astral-reach/hytale0.7/gallery/)

![Astral Reach — Build the next voyage](docs/assets/brand/banner-cinematic-v2.png)

![Kestrel Stack at its full native height](docs/assets/screenshots/kestrel-full.png)

*Native Hytale 0.7.4 development capture: the full-size Kestrel Stack above its launch platform. Latest boarding and landing work remains under test.*

Astral Reach is a space exploration mod in development for **Hytale 0.7.\***. The current playable slice connects home-world nickel and oil shale, four fueled processors, a 25×25 multiblock launch site, ghost-blueprint construction and a paid expedition to Lune for titanium and aluminium.

| Rocket | Height | Maximum width | Launch pad |
| --- | ---: | ---: | ---: |
| Odyssey Heavy | 97.33 blocks | 16.96 blocks | 25×25 blocks |
| Kestrel Stack | 123.19 blocks | 19.31 blocks | 25×25 blocks |

This branch contains the **runtime mod source and original assets**; versioned player documentation lives on `docs`. It does not currently distribute a playable JAR. The guide covers the 0.2.0-dev candidate; local client checks used Hytale 0.7.4. Public documentation is available while the mod remains a development build. See [development status](https://jkswoods.github.io/astral-reach/hytale0.7/status/) before trying a candidate.

The [original model workshop](https://astral-reach-space-workshop.serina610406.chatgpt.site/) is a separate, earlier collection of 66 editable assets and proposed planets. Its model catalogue includes future content beyond the current playable route.

The [versioned player guide](https://jkswoods.github.io/astral-reach/) is maintained on the separate [`docs` branch](https://github.com/jkswoods/astral-reach/tree/docs). In that checkout, run `npm ci --ignore-scripts`, `npm run build` and `npm run check`. Select `docs` when manually dispatching its Pages workflow.

Astral Reach is an independent project and is not affiliated with or endorsed by Hytale or Hypixel Studios. See [credits and notices](https://jkswoods.github.io/astral-reach/hytale0.7/notices/).

## Compatibility and builds

Current source targets Hytale `>=0.7.3 <0.8.0` and uses the `hytale0.7` support branch. See [build and release instructions](https://jkswoods.github.io/astral-reach/hytale0.7/build-and-release/) for the SDK pin, candidate checks, versioned filenames and CurseForge workflow.

## Contributing

See [the contribution guide](CONTRIBUTING.md) for feature and bug-fix PR targets.
Maintainers handle forward/back ports, and support-branch PRs require owner approval.
