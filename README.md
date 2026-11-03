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
| Data generators (`runData`) | ✅ recipes, loot tables, loot modifiers, advancements, tags, villager trades, damage types, enchantments, sounds |
| EMI / CraftTweaker integrations | ❌ disabled for now |

### Notes on 26.3

- **Data generation:** `./gradlew runData` writes `src/generated/resources`. Datapack-registry content
  (recipes, loot tables, advancements, villager trades, damage types, enchantments) is generated through
  NeoForge's `GatherDataRegistryEntriesEvent`. Block states, block/item models, item model definitions,
  worldgen features and biome modifiers are hand-maintained in `src/main/resources`.
- **Canvas signs** use the vanilla sign block entities, because sign bodies are block models in 26.3.
  Canvas signs from old worlds are mapped via registry aliases.
- **Villager trades** are data-driven in 26.3 (`data/farmersdelight/villager_trade`). The config options
  `enableFarmerFDTrades` / `enableWanderingTraderFDTrades` still work: they're applied as load conditions.
- **Config file:** still `config/farmersdelight-common.toml`. Internally it's a `LOCAL` config now,
  because NeoForge 26.3 dropped the `COMMON` type.
- **Cutting board tools:** NeoForge 26.3 removed the vanilla tool abilities (`axe_strip`, `axe_dig`, ...),
  so the built-in recipes use the vanilla tool tags (`#minecraft:axes`, `#minecraft:pickaxes`, ...).
  Add-on recipes that still use `farmersdelight:item_ability` with those ability names keep working
  through a tag fallback.

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
- Translation updates taken from open pull requests to the original repository (MIT, same license):
  mpustovoi (ru_ru, [#1396](https://github.com/vectorwing/FarmersDelight/pull/1396)),
  mykhailolyzhechko (ry_ua, [#1395](https://github.com/vectorwing/FarmersDelight/pull/1395)),
  cerealconjugo (es_mx, [#1326](https://github.com/vectorwing/FarmersDelight/pull/1326), [#1376](https://github.com/vectorwing/FarmersDelight/pull/1376)),
  Kayyz1603 (id_id, [#1350](https://github.com/vectorwing/FarmersDelight/pull/1350)),
  aleimoon (tr_tr, [#1335](https://github.com/vectorwing/FarmersDelight/pull/1335)),
  hikarosato (uk_ua, [#1331](https://github.com/vectorwing/FarmersDelight/pull/1331)),
  Bea-CEO (pt_br, [#1299](https://github.com/vectorwing/FarmersDelight/pull/1299)).

## Contact

Maintainer: Jexxme, minecraft@jeromesommerfeldt.de. For bugs, please use this project's issue tracker.

## License

[MIT](LICENSE), the same license as the original Farmer's Delight, which licenses both its code and
its assets under MIT. vectorwing's copyright notice is kept in `LICENSE` and inside the jar, as the
license requires. "Farmer's Delight" is the name of vectorwing's project. It appears in this project's
name only to say what it continues.
