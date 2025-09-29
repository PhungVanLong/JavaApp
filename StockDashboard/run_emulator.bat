@echo off
echo Starting emulator: Medium Phone...
cd %ANDROID_HOME%\emulator
start emulator -avd "Medium Phone"

echo Waiting for emulator to boot...
%ANDROID_HOME%\platform-tools\adb wait-for-device

echo Installing & running app...
cd ‪F:\JavaApp\StockDashboard
.\gradlew installDebug
adb shell am start -n vn.edu.usth.stockdashboard/.MainActivity