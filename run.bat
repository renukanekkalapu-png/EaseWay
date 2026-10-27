@echo off
echo ========================================================================
echo        Compiling EaseWay — RVCE Mysuru Road Campus Application
echo ========================================================================
if not exist "bin" mkdir bin

javac -d bin src\com\easeway\*.java test\com\easeway\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Compilation successful. Launching EaseWay...
echo ========================================================================
java -cp bin com.easeway.Main
pause
