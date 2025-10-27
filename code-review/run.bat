@echo off
REM Run script for Code Review application

echo Starting Code Review Application...

REM Set paths
set BIN_DIR=bin
set LIB_DIR=lib
set JAVAFX_SDK=..\javafx-sdk-25.0.1

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

REM Build classpath
set CLASSPATH=%BIN_DIR%;%LIB_DIR%\javafx.base.jar;%LIB_DIR%\javafx.controls.jar;%LIB_DIR%\javafx.fxml.jar;%LIB_DIR%\javafx.graphics.jar

REM Add JavaFX bin directory to library path for native libraries
set JAVAFX_BIN=%JAVAFX_SDK%\bin

REM Set JavaFX options
set JAVAFX_OPTS=--enable-native-access=javafx.graphics -Djava.library.path="%JAVAFX_BIN%"

REM Run with JavaFX modules
echo Running application...
java %JAVAFX_OPTS% --module-path "%LIB_DIR%" --add-modules javafx.controls,javafx.fxml -cp "%CLASSPATH%" app.Main

pause
