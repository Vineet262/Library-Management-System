@echo off
REM ============================================================
REM  Library Management System - Build & Run Script (Windows)
REM ============================================================

SET SRC_DIR=src\main\java\library
SET OUT_DIR=out

echo.
echo [1/2] Compiling Java sources...
if not exist %OUT_DIR% mkdir %OUT_DIR%

javac -d %OUT_DIR% %SRC_DIR%\*.java
IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed. Make sure JDK 17+ is installed and on your PATH.
    pause
    exit /b 1
)

echo       Compilation successful.
echo.
echo [2/2] Starting Library Management System...
echo ============================================================
echo.

java -cp %OUT_DIR% library.Main

echo.
echo ============================================================
echo  Session ended.
echo ============================================================
pause
