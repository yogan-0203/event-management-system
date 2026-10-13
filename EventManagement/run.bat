@echo off
REM Event Management System - build & run script (Windows)
REM Uses the bundled OpenJDK and portable MariaDB (auto-downloaded) if available.

setlocal enabledelayedexpansion
set PROJ=%~dp0
set "JDK=%LOCALAPPDATA%\Temp\jdk21\jdk-21.0.12.1+1"
set "MARIA=%LOCALAPPDATA%\Temp\mariadb_root\mariadb-11.4.6-winx64"

REM --- 1) Start the bundled MariaDB server if it is not already running ---
tasklist /fi "imagename eq mysqld.exe" 2>NUL | find /i /n "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo MariaDB already running.
) else (
    if exist "%MARIA%\bin\mysqld.exe" (
        echo Starting bundled MariaDB server...
        start "" /min "%MARIA%\bin\mysqld.exe" --defaults-file="%MARIA%\my.ini"
        timeout /t 6 /nobreak >nul
    ) else (
        echo WARNING: bundled MariaDB not found. The app needs a MySQL server on localhost:3306.
    )
)

REM --- 2) Pick Java (bundled JDK first, else system Java) ---
if exist "%JDK%\bin\java.exe" (
    set "JAVA=%JDK%\bin\java.exe"
    set "JAVAC=%JDK%\bin\javac.exe"
) else (
    set "JAVA=java"
    set "JAVAC=javac"
)

echo Using Java: %JAVA%
echo Compiling...
"%JAVAC%" -encoding UTF-8 -cp "%PROJ%mysql-connector-j-8.4.0.jar" -d "%PROJ%out" "%PROJ%src\*.java"
if errorlevel 1 (
    echo.
    echo Compilation FAILED. Make sure mysql-connector-j-8.4.0.jar is in this folder.
    pause
    exit /b 1
)

echo Starting Event Management System...
"%JAVA%" -cp "%PROJ%out;%PROJ%mysql-connector-j-8.4.0.jar" MainFrame
endlocal
