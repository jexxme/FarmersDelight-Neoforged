# Farmer's Delight (Unofficial 26.3 NeoForge Port)

> **This is an unofficial, community-made port.** It is **not** made, affiliated with or endorsed by
> vectorwing, the author of Farmer's Delight. Please **do not** report bugs of this port to the
> original Farmer's Delight issue tracker or Discord. Report them to this repository instead.

This is a port of [Farmer's Delight](https://github.com/vectorwing/FarmersDelight) by **vectorwing** to
**Minecraft 26.3** with **NeoForge 26.3.0.101+** (Java 25).

Farmer's Delight is a mod that gently expands farming and cooking in Minecraft: cooking pots, stoves,
skillets, cutting boards, new crops (cabbage, tomato, onion, rice), rich soil, meals, feasts, canvas
signs and more. All of the content, the art and the design are by vectorwing and the Farmer's Delight
contributors. This repository only adapts the code and data to the 26.3 APIs.

As of this port, the official mod only goes up to Minecraft 1.21.1 on NeoForge. There is also an official
WIP `26.1` branch that doesn't compile yet. For 26.x the only other option is the Fabric port
[Farmer's Delight Refabricated](https://github.com/MehVahdJukaar/FarmersDelightRefabricated).
Once an official NeoForge release for 26.3 comes out, **use that instead of this port.**

## Status

| Area | Status |
|---|---|
| Build (`gradlew build`) | ✅ |
| Dedicated server start (dev + standalone NeoForge 26.3.0.101 with a real mod set) | ✅ no errors |
| Recipes, loot tables, loot modifiers, tags, advancements, worldgen data | ✅ load without errors |
| Blocks, block entities (cooking pot, stove, cutting board, skillet, basket, cabinets, canvas signs) | ✅ placeable (server smoke test) |
| Client / rendering / GUIs | ⚠️ only briefly tested, see *Known issues* |
| Data generators (`runData`) | ❌ not ported yet (generated JSON is committed instead) |
| EMI / CraftTweaker integrations | ❌ disabled (as in upstream's 26.1 WIP) |
| JEI integration | ✅ compiles against JEI 31.14 (26.3) |

### Known issues / differences to the official mod

- **Datagen is not ported.** The `vectorwing.farmersdelight.data` package is excluded from compilation.
  The generated resources under `src/generated/resources` were taken from Farmer's Delight
  Refabricated's 26.3 branch and translated to NeoForge's JSON format (see *How this port was made*).
- **Canvas signs** now use the vanilla sign block entities, because sign bodies are block models in 26.3.
  Old worlds' canvas sign block entities are mapped via registry aliases.
- **Villager trades** are data-driven in 26.3 (`data/farmersdelight/villager_trade`). Because of this,
  the config options `enableFarmerFDTrades` / `enableWanderingTraderFDTrades` currently have no effect.
- **Config file name:** NeoForge 26.3 dropped the `COMMON` config type. The former common config is now
  a `LOCAL` config: `config/farmersdelight-local.toml`.
- **Skillet item rendering** (food shown in the skillet in hand while it cooks) uses an unfinished
  special renderer inherited from upstream's 26.1 WIP and may look wrong.
- The tool checks of cutting board recipes (`farmersdelight:item_ability`) fall back to the vanilla tool
  tags (`#minecraft:axes`, `#minecraft:pickaxes`, ...). NeoForge 26.3 removed the vanilla tool item
  abilities (`axe_strip`, `axe_dig`, ...).
- The mod ID is still `farmersdelight`, so add-ons and existing worlds keep working. Because of that,
  this port **cannot be installed together with** any other Farmer's Delight build.

## Building

Requirements: JDK 21 or newer to run Gradle. The Gradle toolchain downloads a **JDK 25** automatically
(foojay resolver) if you don't have one installed.

```bash
./gradlew build
```

The jar ends up in `build/libs/FarmersDelight-UnofficialPort-neoforge-26.3-<version>.jar`.

Other useful tasks:

```bash
./gradlew runServer   # dev dedicated server (accept the EULA in run/eula.txt first)
./gradlew runClient   # dev client
```

## How this port was made

- Base: vectorwing's official `26.1` WIP branch, plus the fixes the community continued with in
  [FarmersDelightRefabricated `neoforge/26.1`](https://github.com/MehVahdJukaar/FarmersDelightRefabricated/tree/neoforge/26.1).
  The git history of both is kept.
- 26.1 → 26.3 API changes were guided by the diff of Refabricated's `fabric/latest/26.1` →
  `fabric/latest/26.3` branches, which share the same codebase.
- 26.3-format data and asset JSON (item model definitions, block models, recipes, loot tables, tags,
  worldgen, villager trades) were imported from Refabricated's `fabric/latest/26.3` branch.
  Fabric-specific keys were translated to NeoForge (`fabric:load_conditions` → `neoforge:conditions`,
  `fabric:type` → `neoforge:ingredient_type`, `fabric:any` → `neoforge:compound`). Content that only
  exists in Refabricated was left out.
- The canvas-sign mixins are adapted from Refabricated.

Git remotes used: `upstream` = https://github.com/vectorwing/FarmersDelight, `refab` =
https://github.com/MehVahdJukaar/FarmersDelightRefabricated.

## Credits

- **vectorwing**, original author of Farmer's Delight, and all
  [Farmer's Delight contributors](https://github.com/vectorwing/FarmersDelight/graphs/contributors)
  (code, textures, models, sounds, translations).
- The **Farmer's Delight Refabricated** team (MehVahdJukaar, Cassian, AyOhEe and contributors) for the
  26.x groundwork this port builds on.
- AppleSkin by squeek502, as the reference for the HUD overlay code (credited in the source, as upstream does).

## License

[MIT](LICENSE), the same license as the original Farmer's Delight. Upstream licenses its code *and* its
assets under MIT, and that license notice is kept in this repository and in the built jar.
"Farmer's Delight" is the name of vectorwing's project. It is only used here to describe what this
port is, with a clear *Unofficial Port* label.
