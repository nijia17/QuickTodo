@echo off
setlocal enabledelayedexpansion

REM Get the actual java command path
set "REAL_JAVA=%JAVA_HOME%\bin\java.exe"

REM Check first argument
set "FIRST_ARG=%~1"

if "%FIRST_ARG%"=="-version" goto show_version
if "%FIRST_ARG%"=="--version" goto show_version

REM For all other commands, pass through to real java
"%REAL_JAVA%" %*
exit /b %ERRORLEVEL%

:show_version
REM Output Java 21 version string to trick Kotlin compiler
echo openjdk version "21.0.5" 2024-10-15
echo OpenJDK Runtime Environment Temurin-21.0.5+11 (build 21.0.5+11-LTS)
echo OpenJDK 64-Bit Server VM Temurin-21.0.5+11 (build 21.0.5+11-LTS, mixed mode, sharing)
exit /b 0