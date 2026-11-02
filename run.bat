@echo off

echo ========================================================
echo   Medicine Expiry Alert System - Launching Application
echo   Java Swing Desktop UI + MySQL Persistence
echo ========================================================

where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 'java' runtime was not found in PATH!
    echo Please install Java 17+ and add it to your System PATH.
    pause
    exit /b 1
)

if not exist bin\com\mediguard\Main.class (
    echo [*] Compiled classes not found. Running compilation first...
    call compile.bat
    if errorlevel 1 (
        echo [ERROR] Automatic build failed!
        pause
        exit /b 1
    )
)

echo [*] Launching MediGuard Desktop Application...
start "Medicine Expiry Alert System" java -cp "bin;lib/*;config" com.mediguard.Main

echo [SUCCESS] Application started in separate window.
echo ========================================================
