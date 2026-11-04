@echo off
rem Starts a dev client with your client mod set and joins the playtest server directly.
cd /d "%~dp0.."
call gradlew.bat runPlaytestClient
