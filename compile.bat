@echo off

echo ========================================================
echo   Medicine Expiry Alert System - Compiling Project
echo   Java Swing + MySQL (JDBC) - KTU OOP Practical Project
echo ========================================================

where javac >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 'javac' compiler was not found in PATH!
    echo Please install JDK 17+ and add it to your System PATH.
    pause
    exit /b 1
)

if not exist bin mkdir bin

echo [*] Compiling all Java source files...
javac -encoding UTF-8 -cp "lib/*" -d "bin" src\com\mediguard\*.java src\com\mediguard\model\*.java src\com\mediguard\database\*.java src\com\mediguard\dao\*.java src\com\mediguard\util\*.java src\com\mediguard\ui\*.java

if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [SUCCESS] Compilation finished successfully!
echo Classes generated inside 'bin' directory.
echo ========================================================
