# Rockets and scale

These ships are deliberately large. Plan the launch site around the complete silhouette, including boosters and upper fins.

| Rocket | Height | Maximum width | Maximum depth | Role |
| --- | ---: | ---: | ---: | --- |
| Odyssey Heavy | 97.33 blocks | 16.96 blocks | 8.62 blocks | First home-world-funded launcher |
| Kestrel Stack | 123.19 blocks | 19.31 blocks | 12.90 blocks | Larger reusable stack; requires a Thermal Shield |

<figure><a href="../assets/diagrams/rocket-scale.svg"><img src="../assets/diagrams/rocket-scale.svg" alt="Odyssey and Kestrel schematic profiles on a shared block-height axis, with a tiny 1.8-block reference"></a><figcaption>A shared scale makes the difference explicit. The schematic profiles use actual model bounds at four pixels per block. The 1.8-block reference is about seven pixels high.</figcaption></figure>

## Odyssey Heavy

A copper-orange core, two ivory strap-on boosters, crew capsule and escape tower give Odyssey its heavy-launcher silhouette. Its initial parts and fuel come from the home-world chain. It reaches the current paid Lune route without destination minerals.

<figure><a href="../assets/screenshots/odyssey-full.png"><img src="../assets/screenshots/odyssey-full.png" alt="Odyssey Heavy's full 97-block native silhouette on the wide launch platform"></a><figcaption>Odyssey at its complete native height. The 25×25 pad and separate console sit beneath the engine cluster. Native 0.2.0-dev launch-platform candidate, Hytale 0.7.4.</figcaption></figure>

## Kestrel Stack

Kestrel combines a silver booster with a dark-tiled orbital ship, broad flaps, upper fins and multiple engine bells. Its widest point is **19.31 blocks**, including those upper fins. The pad leaves at least **2.84 blocks** of service width on either side.

<figure><a href="../assets/screenshots/kestrel-player-scale.png"><img src="../assets/screenshots/kestrel-player-scale.png" alt="A player avatar standing near Kestrel's large lower stage and platform"></a><figcaption>A ground-level player reference beside the lower stage. Perspective makes nearby objects larger; use the dimension diagram above for a true shared scale.</figcaption></figure>

The new flight candidate gives Kestrel a separate booster and upper ship. Its locally tested sequence shows separation in space, the booster's flip and home-pad landing, then the orbital ship in space before navigation opens. The latest 90-second flight adds a longer ballistic fall and braking approach. Human visual and comfort acceptance remain pending.

## Current flight limits

Both supported plans currently serve Lune. The spacecraft use decorative full-size exteriors and a compact control anchor; there is no walkable cabin or aerodynamic freeflight. Automatic passenger boarding and upper-stage landing are being tested in the latest candidate. The new third-person ascent and orbital-navigation candidate is described on [development status](status.md).

The model workshop also contains the independent Kestrel upper ship, Peregrine explorer, Wren lander and additional infrastructure. Their presence in the art catalogue does not make them selectable playable vehicles. Peregrine is wider than the supported 25-block pad.
