# Farmer's Delight Neoforged

A maintained NeoForge continuation of [Farmer's Delight](https://www.curseforge.com/minecraft/mc-mods/farmers-delight)
by **vectorwing**, a cozy farming and cooking expansion for Minecraft: cooking pots, stoves, skillets,
cutting boards, new crops, rich soil, meals and feasts.

> Independent project by **Jexxme**, not affiliated with or endorsed by vectorwing.
> Please report bugs here, not on the original Farmer's Delight tracker or
> [Discord](https://discord.gg/eFsz5SK).

| Minecraft | NeoForge | Branch |
|---|---|---|
| 26.3 | 26.3.0.101+ | `26.3` |
| 26.2 | 26.2.0.93+ | `26.2` |
| 26.1.2 | 26.1.2.126+ | `26.1.2` |
| 26.1, 26.1.1 | 26.1.0.19-beta to 26.1.1 (betas) | `26.1.1` |

Minecraft and NeoForge changed too many APIs within 26.x for one jar, so each row has its own branch and jar
(`FarmersDelight-Neoforged-<minecraft>-<version>.jar`). All of them need Java 25 and carry the same version
and fixes. Recipes and tags that use blocks only found in Minecraft 26.3 (poplar wood, shelf mushrooms, wool
slabs and stairs) are only in the 26.3 build.

Versions follow the Farmer's Delight content version plus a build number, and the mod ID stays
`farmersdelight`, so existing worlds and add-ons keep working. It can't be installed alongside another
Farmer's Delight build. JEI is supported. EMI and CraftTweaker support return once those mods are
released for 26.x.

## Building

JDK 21+ for Gradle; a JDK 25 toolchain is downloaded automatically.

```bash
./gradlew build             # jar in build/libs/
./gradlew runData           # regenerate src/generated/resources
./gradlew runGameTestServer # automated in-world tests
```

## Credits

- **vectorwing** and all [Farmer's Delight contributors](https://github.com/vectorwing/FarmersDelight/graphs/contributors):
  the original mod, its code, art, sounds and translations.
- The [Farmer's Delight Refabricated](https://github.com/MehVahdJukaar/FarmersDelightRefabricated) team
  (MehVahdJukaar, Cassian, AyOhEe and contributors) for the 26.x groundwork.
- squeek502 (AppleSkin), the reference for the HUD overlay code.
- Translation updates from open upstream pull requests: mpustovoi (ru_ru, [#1396](https://github.com/vectorwing/FarmersDelight/pull/1396)),
  mykhailolyzhechko (ry_ua, [#1395](https://github.com/vectorwing/FarmersDelight/pull/1395)),
  cerealconjugo (es_mx, [#1326](https://github.com/vectorwing/FarmersDelight/pull/1326), [#1376](https://github.com/vectorwing/FarmersDelight/pull/1376)),
  Kayyz1603 (id_id, [#1350](https://github.com/vectorwing/FarmersDelight/pull/1350)),
  aleimoon (tr_tr, [#1335](https://github.com/vectorwing/FarmersDelight/pull/1335)),
  hikarosato (uk_ua, [#1331](https://github.com/vectorwing/FarmersDelight/pull/1331)),
  Bea-CEO (pt_br, [#1299](https://github.com/vectorwing/FarmersDelight/pull/1299)).

## License

[MIT](LICENSE), like the original. vectorwing's copyright notice is kept in `LICENSE` and in the jar.
"Farmer's Delight" is vectorwing's project name and is used here only to say what this continues.

Contact: Jexxme, minecraft@jeromesommerfeldt.de
