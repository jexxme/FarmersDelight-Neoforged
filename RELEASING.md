# Releasing

A release only goes out when every box below is ticked. A build that compiles and a server that boots are **not** evidence that the mod works: most 26.x bugs (infinite food, duplication, items lost on break, crash on join) passed both.

## 1. Automated (CI must be green on the release commit)

- [ ] `./gradlew build`: compiles; Error Prone `MissingOverride` (every override has `@Override`, so hooks removed by Minecraft/NeoForge break the build) and `checkForbiddenPatterns` (see `build.gradle`) pass
- [ ] `./gradlew runData` leaves `src/generated` unchanged
- [ ] `./gradlew runGameTestServer`: all tests pass, **and** the log has no `ERROR` lines

Locally on Windows, use the same commands with `gradlew.bat`.

## 2. Playtest on a real server with a real client (see `playtest-template/README.md`)

Stop the server and run `update-jar.bat` first, so the server runs the jar you are about to release.

- [ ] `start-server.bat`, then `start-client.bat`: the client **joins** the server without a crash or disconnect (recipe/data sync)
- [ ] Walk the showcase (built on first join; in an old world run `/function fd_playtest:build`): every block renders, nothing is missing a texture
- [ ] The item gallery next to it (`/function fd_playtest:gallery`): every item renders, including the skillet (held and flipping)
- [ ] Cooking pot: open the GUI, cook a meal on the stove, serve it with a bowl, shift-click the output out
- [ ] Cutting board: cut with a knife, an axe and shears; a wrong tool does nothing
- [ ] Skillet: cook on a stove, then flip it in your hand
- [ ] Break a pot, a board, a skillet, a stove, a basket and a cabinet with items inside: everything drops, nothing duplicates
- [ ] A hopper feeds the pot from the top and from the side, and a hopper below pulls out the meal
- [ ] JEI: cooking and cutting categories show recipes, and the recipe buttons work
- [ ] Log out and back in: the contents of the blocks are still there
- [ ] `logs/latest.log` on the server and on the client: no `ERROR` or exception that mentions `farmersdelight`

## 3. Compatibility with the server mod set

- [ ] The playtest server runs the full server mod list (BOP, TerraBlender, Tectonic, Lithostitched, Storage Drawers, Traveler's Backpack, Easy Villagers, Easy Anvils/Magic, Puzzles Lib, JEI) and the client adds Sodium, Iris and Lithium: no crash, no `ERROR` from the mod
- [ ] Create a new world: wild crops generate (BOP biomes included)

## 4. Every 26.x branch

- [ ] Every fix made on `26.3` since the last release is ported to `26.2`, `26.1.2` and `26.1.1` (`git log 26.3` vs each branch), and CI is green on all four
- [ ] Each backport jar got a client test too: join, signs, skillet model, JEI (sections 2 and 3, at least once per branch)

## 5. Publish

- [ ] All four jars share one version, so each upload gets exactly its own Minecraft versions: the `26.1.1` jar is for 26.1 and 26.1.1, the `26.1.2` jar only for 26.1.2, and so on
- [ ] Bump `mod_version` in `gradle.properties` (`<upstream version>+neoforged.<n>`) and add an entry to `changelog.md`
- [ ] Tag the commit (`v26.3-<mod_version>`) and attach the CI-built jar (`build/libs/FarmersDelight-Neoforged-*.jar`)
- [ ] Modrinth/CurseForge page: says it is a continuation of vectorwing's Farmer's Delight under MIT, that it is **not** the official mod, and links the original
