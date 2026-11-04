@echo off
rem Rebuilds the mod and puts the new jar on the playtest server (stop the server first).
cd /d "%~dp0.."
call gradlew.bat build || exit /b 1
del /q "playtest\server\mods\FarmersDelight-Neoforged-*.jar" 2>nul
for %%F in ("build\libs\FarmersDelight-Neoforged-*.jar") do copy /y "%%F" "playtest\server\mods\" >nul
echo Updated playtest\server\mods
