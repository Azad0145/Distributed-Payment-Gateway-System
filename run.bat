@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo ERROR: JDK not found. Install JDK 11 or newer and ensure javac is on PATH.
  pause
  exit /b 1
)
if not exist out mkdir out
echo Compiling Payment Gateway desktop application...
javac -encoding UTF-8 -d out -sourcepath src src\gui\PaymentGatewayApp.java
if errorlevel 1 (
  echo Compilation failed. Please check the error above.
  pause
  exit /b 1
)
echo Launching Payment Gateway desktop GUI...
java -cp out gui.PaymentGatewayApp
if errorlevel 1 pause
endlocal
