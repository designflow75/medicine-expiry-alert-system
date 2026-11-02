@echo off

echo ========================================================
echo   Medicine Expiry Alert System - Build and Run
echo ========================================================

call compile.bat
if errorlevel 1 (
    echo [ERROR] Build step failed. Cannot start application.
    pause
    exit /b 1
)

call run.bat
