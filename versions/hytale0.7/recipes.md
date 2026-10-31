---
title: "Recipes"
---

# Recipes {#recipes}

Quantities below are the current development recipes. Iron and copper entries mean stock **bars**. Processors also consume gathered stock Fuel resources; their times below exclude pauses.

## Benches and machines {#benches-and-machines}

| Output | Inputs | Craft at |
| --- | --- | --- |
| 1 Astral Assembly Bench | 6 iron + 4 copper | Stock Workbench |
| 1 Orbital Smelter | 8 iron + 4 copper | Assembly Bench |
| 1 Component Press | 10 iron + 4 copper | Assembly Bench |
| 1 Fuel Refinery | 8 iron + 6 copper | Assembly Bench |
| 1 Propellant Conditioner | 8 iron + 8 copper | Assembly Bench |

## Materials and fuel {#materials-and-fuel}

| Output | Inputs | Machine | Time |
| --- | --- | --- | ---: |
| 1 nickel ingot | 1 raw nickel | Smelter | 8 s |
| 1 titanium ingot | 1 raw titanium | Smelter | 10 s |
| 1 aluminium ingot | 1 raw aluminium | Smelter | 8 s |
| 2 Nickel-steel Frame Plates | 2 iron + 1 nickel ingot | Press | 6 s |
| 1 Copper Coil | 2 copper | Press | 4 s |
| 1 crude hydrocarbon | 2 oil shale | Refinery | 8 s |
| 2 Empty Fuel Cells | 1 nickel ingot + 1 copper | Assembly Bench | 4 s |
| 1 Conditioned Propellant Cell | 1 crude hydrocarbon + 1 empty cell | Conditioner | 8 s |
| 1 Thermal Shield | 12 titanium ingots + 8 aluminium ingots | Press | 20 s |

## Rocket parts and launch site {#rocket-parts-and-launch-site}

| Output | Inputs | Craft at |
| --- | --- | --- |
| 1 Pressure Hull Section | 4 frame plates + 1 coil | Assembly Bench |
| 1 Orbital Engine Part | 4 frame plates + 2 coils + 2 nickel ingots | Assembly Bench |
| 1 Flight Computer | 2 frame plates + 4 coils + 2 copper | Assembly Bench |
| 1 Launch Pad Module | 8 iron + 2 nickel ingots | Assembly Bench |
| 1 Launch Construction Console | 4 frame plates + 2 coils + 2 copper | Assembly Bench |

A new launch site uses **25 modules**, making a **25×25** deck. Those modules alone cost **200 iron bars + 50 nickel ingots**.

## Ghost-blueprint construction {#ghost-blueprint-construction}

Select a plan at the separate construction console, then **Supply needed parts**. A complete, clear and loaded site builds the rocket in **10 seconds**. The finished tank is empty.

| Material | Odyssey Heavy | Kestrel Stack |
| --- | ---: | ---: |
| Pressure Hull Sections | 6 | 6 |
| Orbital Engine Parts | 4 | 6 |
| Flight Computers | 1 | 2 |
| Nickel-steel Frame Plates | 16 | 24 |
| Thermal Shield | — | 1 |

Including the bench, four processors, Odyssey, 25 modules, console and nine initial full cells takes **302 iron bars, 75 copper bars, 94 raw nickel and 18 oil shale**, with one spare empty casing. Add the Suit Workbench, suit, oxygen equipment, tools and processor fuel; those requirements are additional. The first launch uses home-world resources throughout.

The older Assembly recipe for Odyssey and the **8×8 Launch Deck** recipe (4 iron bars + 1 nickel ingot) remain for older items and saves. Use the [wide launch platform](launch-platform.md) for a new yard.

## Expedition blueprint recipes {#expedition-blueprint-recipes}

Private development additions. Complete the matching [lunar expedition](rescue-and-expeditions.md#unlock-expedition-blueprints) to learn each crafting recipe. Materials are still required.

| Output | Inputs | Craft at | Time |
| --- | --- | --- | ---: |
| 1 Empty High-Capacity Oxygen Tank | 2 Empty Oxygen Canisters + 4 Aluminium Ingots + 2 Titanium Ingots + 2 Copper Coils | Suit Workbench · Survey equipment | 10 s |
| 1 full High-Capacity Oxygen Tank | 1 matching empty tank + normal collector fuel | Oxygen Collector on Orbis | 60 s |
| 1 Lunar Rover kit | 12 Frame Plates + 8 Aluminium Ingots + 6 Titanium Ingots + 4 Copper Coils + 4 Nickel Ingots + 1 Flight Computer | Assembly Bench · Station equipment | 20 s |

The larger tank holds forty minutes of normal breathing. A newly crafted rover is empty: each existing Beacon Cell supplies fifteen minutes of moving power, with room for two cells. Native 0.7.5 checks cover paid crafting, filling, used-casing crafting, larger blueprint cards and normal equipment/recipe reload. The latest native rover attachment keeps the parked driver visible, but mounted driving sync and deliberate exit remain unfinished. Full visual/listening, pickup/redeployment, Moon motion and passenger acceptance are pending.
