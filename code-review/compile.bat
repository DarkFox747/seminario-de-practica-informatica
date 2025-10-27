@echo off
REM Compile script for Code Review application

echo Compiling Code Review Application...

REM Set paths
set SRC_DIR=src
set OUT_DIR=bin
set LIB_DIR=lib

REM Create output directory if it doesn't exist
if not exist %OUT_DIR% mkdir %OUT_DIR%

REM Build classpath with all JavaFX JARs and MySQL Connector
set CLASSPATH=%LIB_DIR%\javafx.base.jar;%LIB_DIR%\javafx.controls.jar;%LIB_DIR%\javafx.fxml.jar;%LIB_DIR%\javafx.graphics.jar;%LIB_DIR%\mysql-connector-j-8.0.33.jar

REM Compile all Java files
echo Compiling source files...
javac -d %OUT_DIR% -cp "%CLASSPATH%" -sourcepath %SRC_DIR% %SRC_DIR%\app\Main.java %SRC_DIR%\app\config\AppConfig.java %SRC_DIR%\app\domain\value\*.java %SRC_DIR%\app\domain\entity\*.java %SRC_DIR%\app\domain\port\*.java %SRC_DIR%\app\infra\tx\*.java %SRC_DIR%\app\infra\persistence\*.java %SRC_DIR%\app\infra\integration\*.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo Copying resources...
    if not exist %OUT_DIR%\resources mkdir %OUT_DIR%\resources
    copy %SRC_DIR%\resources\*.* %OUT_DIR%\resources\ > nul
    echo Output directory: %OUT_DIR%
) else (
    echo Compilation failed!
    exit /b 1
)

pause
