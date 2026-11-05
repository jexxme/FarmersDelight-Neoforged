# Upstream issue review

Review of the open issues and pull requests in
[vectorwing/FarmersDelight](https://github.com/vectorwing/FarmersDelight/issues) (23 open as of 2026-11-03),
and what they mean for Farmer's Delight Neoforged (Minecraft 26.3).

## Fixed in Farmer's Delight Neoforged

| Upstream | Problem | Fix here |
|---|---|---|
| [#1412](https://github.com/vectorwing/FarmersDelight/issues/1412) | Rope Fence (Gate) items missing from `#minecraft:fences` / `#minecraft:fence_gates` | Added to the vanilla item tags (datagen `ItemTags`) |
| [#1411](https://github.com/vectorwing/FarmersDelight/issues/1411) | Straw from modded grass needs one loot modifier per block | New block tag `#farmersdelight:drops_straw` (short + tall grass by default). `straw_from_grass` uses it; `straw_from_tall_grass` was merged into it |
| [#1407](https://github.com/vectorwing/FarmersDelight/issues/1407) | Stove can't be lit with modded flint and steels | Stove accepts any item with the `firestarter_light` ability (incl. fire charges, which get consumed) |
| [#1404](https://github.com/vectorwing/FarmersDelight/issues/1404) | Farmers try to compost FD seeds but nothing happens | Cabbage and tomato seeds added to NeoForge's `villager_compostables` data map |
| [#1391](https://github.com/vectorwing/FarmersDelight/issues/1391) | `farmersdelight:add_loot_table` field renamed `lootTable` → `table` | Both field names are accepted |
| [#1384](https://github.com/vectorwing/FarmersDelight/issues/1384) | Shift-clicking a meal out of the Cooking Pot doesn't award the "crafted" stat | Quick-move now triggers the craft callback, like vanilla furnaces |
| [#1397](https://github.com/vectorwing/FarmersDelight/pull/1397) (PR, 1.20) | Tool-ability ingredient rescans the item registry | The 26.x `ItemAbilityIngredient` cached a `Stream`, which can only be read once. It now caches a list |
| Translation PRs: [#1396](https://github.com/vectorwing/FarmersDelight/pull/1396) ru_ru, [#1395](https://github.com/vectorwing/FarmersDelight/pull/1395) Rusyn, [#1376](https://github.com/vectorwing/FarmersDelight/pull/1376) / [#1326](https://github.com/vectorwing/FarmersDelight/pull/1326) es_mx, [#1350](https://github.com/vectorwing/FarmersDelight/pull/1350) id_id, [#1335](https://github.com/vectorwing/FarmersDelight/pull/1335) tr_tr, [#1331](https://github.com/vectorwing/FarmersDelight/pull/1331) uk_ua, [#1299](https://github.com/vectorwing/FarmersDelight/pull/1299) pt_br | Merged key by key into the 26.3 language files (only keys that still exist), translators credited in the README |

## Not applicable / already covered

| Upstream | Notes |
|---|---|
| [#1398](https://github.com/vectorwing/FarmersDelight/pull/1398) (PR) | Forge 1.20 overlay API, not relevant for NeoForge 26.3 |
| [#1375](https://github.com/vectorwing/FarmersDelight/pull/1375), [#1374](https://github.com/vectorwing/FarmersDelight/pull/1374), [#1355](https://github.com/vectorwing/FarmersDelight/pull/1355) (PRs) | 26.1/26.2 porting work. This project builds on #1374's branch and goes further to 26.3 |
| [#1399](https://github.com/vectorwing/FarmersDelight/issues/1399) | Question about a 26.x release, answered by this project |

## Open / not addressed yet

| Upstream | Notes |
|---|---|
| [#1385](https://github.com/vectorwing/FarmersDelight/issues/1385), [#1386](https://github.com/vectorwing/FarmersDelight/pull/1386) (PR) | EMI info recipes. Blocked: EMI (and CraftTweaker) have no 26.x release yet, so there is no API to build against |
| [#1390](https://github.com/vectorwing/FarmersDelight/issues/1390) | Tomatoes on Supplementaries sticks drop only XP. The report involves a right-click-harvest mod (Actually Harvest) plus Supplementaries; neither exists for 26.3, so it can't be reproduced. FD's own loot table and right-click harvest for hanging tomatoes were checked and are correct. Related fix: the "rope permanence" config was dead on 26.x (`onRemove` no longer exists) and works again |
