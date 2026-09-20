@echo off
setlocal

set THREADS=%1
if "%THREADS%"=="" set THREADS=500

set RAMPUP=%2
if "%RAMPUP%"=="" set RAMPUP=60

set DURATION=%3
if "%DURATION%"=="" set DURATION=300

set HOST=%4
if "%HOST%"=="" set HOST=localhost

set PORT=%5
if "%PORT%"=="" set PORT=8080

set TIMESTAMP=%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set RESULTS_DIR=results_%TIMESTAMP%
set JTL_FILE=%RESULTS_DIR%\results.jtl
set HTML_DIR=%RESULTS_DIR%\html-dashboard

echo ==================================================================
echo   EAMS High-Concurrency Load Test (JMeter CLI Runner)
echo ==================================================================
echo Target Host     : http://%HOST%:%PORT%
echo Virtual Users   : %THREADS% concurrent threads
echo Ramp-up Time    : %RAMPUP% seconds
echo Duration        : %DURATION% seconds
echo Output Folder   : %RESULTS_DIR%
echo ==================================================================

mkdir "%RESULTS_DIR%" 2>nul
mkdir "%HTML_DIR%" 2>nul

jmeter -n -t eams-load-test-500-1000-users.jmx -l "%JTL_FILE%" -e -o "%HTML_DIR%" -Jhost=%HOST% -Jport=%PORT% -Jthreads=%THREADS% -Jrampup=%RAMPUP% -Jduration=%DURATION%

if %ERRORLEVEL% equ 0 (
    echo.
    echo ==================================================================
    echo   LOAD TEST COMPLETED SUCCESSFULLY!
    echo ==================================================================
    echo Dashboard report generated at: %HTML_DIR%\index.html
    start "" "%HTML_DIR%\index.html"
) else (
    echo [!] JMeter test failed or was interrupted.
)

endlocal
