@echo off
REM Run script for Code Review application

echo Starting Code Review Application...

REM Set paths
set BIN_DIR=bin
set LIB_DIR=lib
set NATIVE_DIR=lib\native

REM Check if compiled
if not exist %BIN_DIR%\app\Main.class (
    echo Application not compiled. Running compile.bat first...
    call compile.bat
    if %ERRORLEVEL% NEQ 0 (
        echo Compilation failed. Cannot run application.
        pause
        exit /b 1
    )
)

REM Build classpath with MySQL connector
set CLASSPATH=%BIN_DIR%;%LIB_DIR%\javafx.base.jar;%LIB_DIR%\javafx.controls.jar;%LIB_DIR%\javafx.fxml.jar;%LIB_DIR%\javafx.graphics.jar;%LIB_DIR%\mysql-connector-j-8.0.33.jar

REM Run with JavaFX modules and native libraries
echo Running application...
java --module-path "%LIB_DIR%" --add-modules javafx.controls,javafx.fxml -Djava.library.path="%NATIVE_DIR%" -cp "%CLASSPATH%" app.Main

pause
