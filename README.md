# Farmer's Delight Neoforged

**Farmer's Delight Neoforged** is a maintained NeoForge continuation of
[Farmer's Delight](https://github.com/vectorwing/FarmersDelight) by **vectorwing**. It is kept up to date
with new Minecraft versions.

Farmer's Delight gently expands farming and cooking in Minecraft: cooking pots, stoves, skillets,
cutting boards, new crops (cabbage, tomato, onion, rice), rich soil, hearty meals, feasts, canvas signs
and more. The content, art and design come from vectorwing and the Farmer's Delight contributors.
Farmer's Delight Neoforged keeps that mod alive on current Minecraft and NeoForge versions.

> Farmer's Delight Neoforged is an independent project maintained by **Jexxme**. It is not made,
> affiliated with or endorsed by vectorwing. Please report bugs to this project's issue tracker, **not**
> to the original Farmer's Delight issue tracker or Discord.

## Supported versions

| Minecraft | Loader | Branch | Farmer's Delight content | Status |
|---|---|---|---|---|
| 26.3 | NeoForge 26.3.0.101+ (Java 25) | `26.3` | 1.3.3 | ✅ first release |

New Minecraft versions get their own branch. Version numbers follow the Farmer's Delight content
version plus a Neoforged build number, e.g. `1.3.3+neoforged.1`. That way add-ons that depend on a
minimum Farmer's Delight version keep working.

The mod ID stays `farmersdelight`, so **existing worlds and Farmer's Delight add-ons keep working.**
For the same reason it can't be installed together with any other Farmer's Delight build.

## Status (26.3)

| Area | Status |
|---|---|
| Build (`gradlew build`) | ✅ |
| Dedicated server (dev + standalone NeoForge 26.3.0.101 with a real mod set) | ✅ no errors |
| Recipes, loot tables, loot modifiers, tags, advancements, worldgen data | ✅ load without errors |
| Blocks and block entities (cooking pot, stove, cutting board, skillet, basket, cabinets, canvas signs) | ✅ |
| Client | ✅ starts without errors; in-game rendering/GUIs not fully tested yet |
| JEI integration | ✅ (JEI 31.14 for 26.3) |
| Data generators (`runData`) | ❌ not ported yet (generated JSON is committed) |
| EMI / CraftTweaker integrations | ❌ disabled for now |

### Known issues and changes in 26.3

- **Datagen is not ported yet.** The `vectorwing.farmersdelight.data` package is excluded from compilation,
  and the 26.3-format JSON under `src/generated/resources` is maintained directly.
- **Canvas signs** use the vanilla sign block entities, because sign bodies are block models in 26.3.
  Canvas signs from old worlds are mapped via registry aliases.
- **Villager trades** are data-driven in 26.3 (`data/farmersdelight/villager_trade`). Because of that,
  the config options `enableFarmerFDTrades` / `enableWanderingTraderFDTrades` currently have no effect.
- **Config file:** NeoForge 26.3 dropped the `COMMON` config type. The settings are now in
  `config/farmersdelight-local.toml`.
- **Skillet in hand:** showing food in the skillet while it cooks uses an unfinished renderer and may look wrong.
- Cutting board tool checks (`farmersdelight:item_ability`) fall back to the vanilla tool tags
  (`#minecraft:axes`, `#minecraft:pickaxes`, ...). NeoForge 26.3 removed the vanilla tool item
  abilities (`axe_strip`, `axe_dig`, ...).

## Building

Requirements: JDK 21 or newer to run Gradle. The Gradle toolchain downloads a **JDK 25** automatically
if none is installed.

```bash
./gradlew build
```

The jar ends up in `build/libs/FarmersDelight-Neoforged-26.3-<version>.jar`.

```bash
./gradlew runServer   # dev dedicated server (set eula=true in run/eula.txt)
./gradlew runClient   # dev client
```

## History

- Based on vectorwing's official Farmer's Delight code, including the WIP `26.1` branch, and on the
  community fixes in [Farmer's Delight Refabricated `neoforge/26.1`](https://github.com/MehVahdJukaar/FarmersDelightRefabricated/tree/neoforge/26.1).
  The full git history of both is kept.
- The 26.1 → 26.3 migration was guided by Refabricated's Fabric 26.1 → 26.3 changes. That project
  shares the same codebase.
- The 26.3-format data and asset JSON was imported from Refabricated's `fabric/latest/26.3` branch.
  Fabric-specific JSON was translated to NeoForge (`fabric:load_conditions` → `neoforge:conditions`,
  `fabric:type` → `neoforge:ingredient_type`, `fabric:any` → `neoforge:compound`).
  Refabricated-only content was left out.
- The canvas-sign mixins are adapted from Refabricated.

Git remotes: `upstream` = https://github.com/vectorwing/FarmersDelight,
`refab` = https://github.com/MehVahdJukaar/FarmersDelightRefabricated.

## Credits

- **vectorwing**, creator of Farmer's Delight, and all
  [Farmer's Delight contributors](https://github.com/vectorwing/FarmersDelight/graphs/contributors)
  (code, textures, models, sounds, translations).
- The **Farmer's Delight Refabricated** team (MehVahdJukaar, Cassian, AyOhEe and contributors) for the
  26.x groundwork.
- squeek502 (AppleSkin), the reference for the HUD overlay code.

## Contact

Maintainer: Jexxme, minecraft@jeromesommerfeldt.de. For bugs, please use this project's issue tracker.

## License

[MIT](LICENSE), the same license as the original Farmer's Delight, which licenses both its code and
its assets under MIT. vectorwing's copyright notice is kept in `LICENSE` and inside the jar, as the
license requires. "Farmer's Delight" is the name of vectorwing's project. It appears in this project's
name only to say what it continues.
