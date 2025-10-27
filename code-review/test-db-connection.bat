@echo off
setlocal enabledelayedexpansion
REM ==========================================
REM Database Connection Test Script
REM ==========================================

echo.
echo ==========================================
echo   TESTING DATABASE CONNECTION
echo ==========================================
echo.

REM Check if project is compiled
if not exist "bin\app\config\AppConfig.class" (
    echo Project not compiled. Running compile.bat first...
    echo.
    call compile.bat
    if %ERRORLEVEL% NEQ 0 (
        echo.
        echo ❌ Compilation failed!
        pause
        exit /b 1
    )
)

REM Set classpath with all JARs
set CLASSPATH=bin
for %%i in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%i

echo Compiling TestConnection.java...
javac -encoding UTF-8 -cp "%CLASSPATH%" -d bin src\TestConnection.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Test compilation failed!
    pause
    exit /b 1
)

echo.
echo Running connection test...
echo.
java -cp "%CLASSPATH%" TestConnection

echo.
pause
