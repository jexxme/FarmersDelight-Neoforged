@echo off
rem Starts the playtest server (your server mod set + Farmer's Delight Neoforged) on localhost:25565.
cd /d "%~dp0..\playtest\server"
set "JAVA=java"
for /d %%J in ("%USERPROFILE%\.gradle\jdks\*25*") do if exist "%%J\bin\java.exe" set "JAVA=%%J\bin\java.exe"
"%JAVA%" -Xmx4G @user_jvm_args.txt @libraries/net/neoforged/neoforge/26.3.0.101/win_args.txt --nogui
pause
