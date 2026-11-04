# Playtest setup

Local playtesting with a real server and real clients. The working copies live in `playtest/` (gitignored);
this folder holds the scripts and the showcase datapack.

| Script | What it does |
|---|---|
| `start-server.bat` | Starts `playtest/server` (NeoForge 26.3.0.101, the Season 4 server mod set + this mod) on `localhost:25565`, offline mode, creative |
| `start-client.bat` | Dev client with the Season 4 client mods (`playtest/client`), joins the local server directly |
| `start-solo.bat` | Dev client with only this mod + JEI (`playtest/solo`), opens the "FD Playtest" singleplayer world directly |
| `update-jar.bat` | Rebuilds the mod and copies the jar to the server (stop the server first) |

`fd_playtest/` is a datapack (installed in both worlds): on first join it builds a showcase at your position:
stoves with cooking pot and skillet, cutting boards, cabinets, feasts and pies, canvas signs, chests with
ingredients, tools and seeds, rich soil farmland with mature crops, tomatoes on ropes, mushroom colonies and grass
for straw. Players start in creative mode (`/gamemode survival` for survival behaviour).

The dev clients run the mod from source, so code changes only need a client restart; the server needs
`update-jar.bat`.
