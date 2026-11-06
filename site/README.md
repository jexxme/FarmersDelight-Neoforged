# Farmer's Delight Neoforged docs

The documentation site for Farmer's Delight Neoforged, built with [Astro](https://astro.build) and
[Starlight](https://starlight.astro.build). It is published to GitHub Pages by `.github/workflows/docs.yml`.

Every number on the site comes from the mod: a dev-only GameTest exports runtime values, scripts read the generated
data, and a small renderer draws item icons and isometric block scenes from the mod's models and textures.

```bash
cd site
npm ci
npm run dev        # http://localhost:4321/FarmersDelight-Neoforged/
npm run build      # static site in dist/
```

After the mod's data changes:

```bash
./gradlew runDocsExport    # in the repo root: writes site/src/data/runtime.json
cd site && npm run data    # regenerates src/data/gen, public/icons, public/renders, public/gui
```

See [AUTHORING.md](AUTHORING.md) for how pages are written and which components exist.
