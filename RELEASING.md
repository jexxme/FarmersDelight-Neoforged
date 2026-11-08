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

- [ ] Every fix made on `26.3` since the last release is ported to `26.2`, `26.1.2` and `26.1` (`git log 26.3` vs each branch), and CI is green on all four
- [ ] Each backport jar got a client test too: join, signs, skillet model, JEI (sections 2 and 3, at least once per branch)

## 5. Publish

The release workflow (`.github/workflows/release.yml`) does the uploading. On each branch:

- [ ] Bump `mod_version` in `gradle.properties` (`<Farmer's Delight version>-<n>`, e.g. `1.3.4-3`; back to `-1` when
      the Farmer's Delight version changes; the same on every branch) and add
      the entry at the top of `changelog.md`: that section becomes the release notes
- [ ] `release_game_versions` lists exactly the Minecraft versions this jar runs on (`26.1` branch: `26.1,26.1.1`)
- [ ] Tag and push: `git tag <minecraft_version>-<mod_version>` (e.g. `26.3-1.3.4-3`), then `git push origin <tag>`.
      The workflow refuses a tag that doesn't match `gradle.properties`, builds and tests again, then publishes

Once, before the first release:

- [ ] Create the Modrinth and CurseForge projects. Their pages say it is a continuation of vectorwing's Farmer's Delight
      under MIT, that it is **not** the official mod, and link the original
- [ ] In the GitHub repository settings, add the variables `MODRINTH_PROJECT_ID` and `CURSEFORGE_PROJECT_ID` and the
      secrets `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN`. Without them the workflow only makes the GitHub release
