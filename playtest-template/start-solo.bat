@echo off
rem Starts a dev client (Farmer's Delight + JEI only) straight into the "FD Playtest" singleplayer world.
cd /d "%~dp0.."
call gradlew.bat runPlaytestSolo
