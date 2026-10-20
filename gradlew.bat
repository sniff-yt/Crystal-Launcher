@echo off
setlocal
set GRADLE_VERSION=8.13
if "%GRADLE_USER_HOME%"=="" set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "BASE=%GRADLE_USER_HOME%\crystal-launcher"
set "DIST=%BASE%\gradle-%GRADLE_VERSION%"
if not exist "%DIST%\bin\gradle.bat" (
  if not exist "%BASE%" mkdir "%BASE%"
  echo Downloading Gradle %GRADLE_VERSION% (first run only)...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $base='%BASE%'; $zip=Join-Path $base 'gradle-%GRADLE_VERSION%-bin.zip'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile $zip; Expand-Archive -LiteralPath $zip -DestinationPath $base -Force; Remove-Item $zip -Force"
  if errorlevel 1 exit /b 1
)
call "%DIST%\bin\gradle.bat" %*
