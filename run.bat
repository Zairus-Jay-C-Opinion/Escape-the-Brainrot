@echo off
rem Compiles src\ into bin\, copies res\ into bin\, and runs Main (Windows).
setlocal enabledelayedexpansion

rem Work from the script's own folder so it runs from anywhere, even with spaces in the path.
cd /d "%~dp0"

if not exist bin mkdir bin
if exist bin\sources.txt del bin\sources.txt

rem List every .java file (quoted, with forward slashes) so javac gets them all in one go.
for /f "delims=" %%f in ('dir /s /b src\*.java') do (
    set "p=%%f"
    set "p=!p:\=/!"
    echo "!p!">> bin\sources.txt
)

javac -d bin @bin\sources.txt || exit /b 1

rem Resources end up on the classpath root (bin\), which is where getResourceAsStream looks.
xcopy /e /i /y /q "res" "bin" >nul

java -cp bin Main
