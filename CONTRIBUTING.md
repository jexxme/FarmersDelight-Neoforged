# Contributing

## Building and testing

JDK 21+ for Gradle; a JDK 25 toolchain is downloaded automatically.

```bash
./gradlew build             # jar in build/libs/, plus the static checks
./gradlew runData           # regenerate src/generated/resources (commit the result)
./gradlew runGameTestServer # in-world tests: gameplay, item conservation, regressions
```

CI runs all three on every push and pull request, and also fails on any `ERROR` in the test log.
To play-test on a real server and client, see [playtest-template/README.md](playtest-template/README.md).

## Minecraft versions

Minecraft and NeoForge change too many APIs within 26.x for one jar, so each supported version has its own
branch, like the original Farmer's Delight:

| Branch | Minecraft | NeoForge |
|---|---|---|
| `26.3` (default) | 26.3 | 26.3.0.101+ |
| `26.2` | 26.2 | 26.2.0.93+ |
| `26.1.2` | 26.1.2 | 26.1.2.126+ |
| `26.1` | 26.1, 26.1.1 | 26.1.0.19-beta to 26.1.1 |

- Open pull requests against `26.3`. Fixes are then ported down with `git cherry-pick -x <commit>` to each
  older branch, adapted where the API differs, and only merged there when that branch's CI is green.
- Content that needs a newer Minecraft (blocks that don't exist in older versions) stays on the branches that have it.
- `README.md`, `CONTRIBUTING.md` and `RELEASING.md` are the same on every branch: change them on `26.3`, then copy them.

## Versions and releases

The mod version is the Farmer's Delight content version plus a build number (`1.3.3+neoforged.1`), the same on
every branch. A release is tagged per branch as `<minecraft_version>-<mod_version>`, for example
`26.3-1.3.3+neoforged.1`, which is also the version in the jar name. Pushing the tag runs the release
workflow, which builds and tests again and publishes the jar. Checklist: [RELEASING.md](RELEASING.md).
