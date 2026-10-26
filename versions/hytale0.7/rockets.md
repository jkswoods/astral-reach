---
title: "Rockets and scale"
---

# Rockets and scale {#rockets-and-scale}

These ships are deliberately large. Plan the launch site around the complete silhouette, including boosters and upper fins.

| Rocket | Height | Maximum width | Maximum depth | Role |
| --- | ---: | ---: | ---: | --- |
| Odyssey Heavy | 97.33 blocks | 16.96 blocks | 8.62 blocks | First home-world-funded launcher |
| Kestrel Stack | 123.19 blocks | 19.31 blocks | 12.90 blocks | Larger reusable stack; requires a Thermal Shield |

<figure><a href="/assets/hytale0.7/diagrams/rocket-scale.svg"><img src="/assets/hytale0.7/diagrams/rocket-scale.svg" alt="Odyssey and Kestrel schematic profiles on a shared block-height axis, with a tiny 1.8-block reference"></a><figcaption>A shared scale makes the difference explicit. The schematic profiles use actual model bounds at four pixels per block. The 1.8-block reference is about seven pixels high.</figcaption></figure>

## Odyssey Heavy {#odyssey-heavy}

A copper-orange core, two ivory strap-on boosters, crew capsule and escape tower give Odyssey its heavy-launcher silhouette. Its initial parts and fuel come from the home-world chain. It reaches the current paid Lune route without destination minerals.

<figure><a href="/assets/hytale0.7/screenshots/odyssey-full.png"><img src="/assets/hytale0.7/screenshots/odyssey-full.png" alt="Odyssey Heavy's full 97-block native silhouette on the wide launch platform"></a><figcaption>Odyssey at its complete native height. The 25×25 pad and separate console sit beneath the engine cluster. Native launch-platform development capture, Hytale 0.7.4.</figcaption></figure>

## Kestrel Stack {#kestrel-stack}

Kestrel combines a silver booster with a dark-tiled orbital ship, broad flaps, upper fins and multiple engine bells. Its widest point is **19.31 blocks**, including those upper fins. The pad leaves at least **2.84 blocks** of service width on either side.

<figure><a href="/assets/hytale0.7/screenshots/kestrel-player-scale.png"><img src="/assets/hytale0.7/screenshots/kestrel-player-scale.png" alt="A player avatar standing near Kestrel's large lower stage and platform"></a><figcaption>A ground-level player reference beside the lower stage. Perspective makes nearby objects larger; use the dimension diagram above for a true shared scale.</figcaption></figure>

The new flight candidate gives Kestrel a separate booster and upper ship. Its locally tested sequence shows separation in space, the booster's flip and home-pad landing, then the orbital ship in space before navigation opens. The latest 90-second flight adds a longer ballistic fall and braking approach. Human visual and comfort acceptance remain pending.

## Current flight limits {#current-flight-limits}

Both supported plans currently serve Lune. The spacecraft use decorative full-size exteriors and a compact control anchor; there is no walkable cabin or aerodynamic freeflight. Automatic passenger boarding and upper-stage landing are being tested in the latest candidate. The new third-person ascent and orbital-navigation candidate is described on [development status](status.md).

The model workshop also contains the independent Kestrel upper ship, Peregrine explorer, Wren lander and additional infrastructure. Their presence in the art catalogue does not make them selectable playable vehicles. Peregrine is wider than the supported 25-block pad.


## Select a destination in orbit {#select-a-destination-in-orbit}

The redesigned **Destinations** screen becomes available after reaching space. Select a route in the list or click a planet on the top-down system chart. Its details show resources, equipment, available oxygen and protected homeward fuel. Use the single travel button to land on **Lune** or return to **Orbis**. Later bodies remain **Future route** entries with travel disabled.

The latest private candidate keeps the framed top-down system chart. Select a planet directly or use the route list. Its arrow controls pan the magnified chart, **+ / −** change the view from **100% to 225%**, and **Fit** restores the full system. Pan controls become available after zooming in. These updated controls are awaiting their in-game check.

Hytale 0.7 custom pages do not expose direct chart drag or wheel callbacks. These are visible navigation controls; direct map gestures are not currently provided.

<figure><a href="/assets/hytale0.7/screenshots/destinations-ui-private.png"><img src="/assets/hytale0.7/screenshots/destinations-ui-private.png" alt="Native top-down Destinations panel with Lune selected"></a><figcaption>Current private interface in Hytale 0.7.4. The separate 3D option is removed; later routes remain disabled. Appearance review continues.</figcaption></figure>

During a cancellable launch, **ABORT LAUNCH** stops the active departure using its existing fuel-restoration handling. The private interface now displays a large red button, checked in Hytale0.7.4 using a temporary no-journey overlay. The updated button has not yet had a fresh paid-launch cancellation test.

Close the screen to remain in orbit. `/astral-map` reopens navigation without charging more fuel. Closing navigation does not spend the return reserve.

<figure><a href="/assets/hytale0.7/screenshots/abort-launch-ui-private.png"><img src="/assets/hytale0.7/screenshots/abort-launch-ui-private.png" alt="Large red ABORT LAUNCH button in native Hytale"></a><figcaption>Actual private Hytale0.7.4 rendering in a four-second diagnostic overlay. This capture demonstrates the button’s appearance; it is not a paid-flight cancellation.</figcaption></figure>

The revised interface has representative native selection, clean item icons and paid travel checks on Hytale0.7.4. Map gestures and further visual refinement remain in development; earlier gallery screenshots show the previous layout.
