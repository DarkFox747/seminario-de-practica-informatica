@echo off
REM Clean build artifacts

echo Cleaning build artifacts...

if exist bin (
    rmdir /s /q bin
    echo Deleted bin directory
)

echo Clean complete!
pause
