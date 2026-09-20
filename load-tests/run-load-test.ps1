# ==============================================================================
# EAMS Load Test Automation Runner (PowerShell)
# 500-1000 Concurrent Users Benchmark with Automated HTML Dashboard Reporting
# ==============================================================================

param (
    [int]$Threads = 500,
    [int]$RampUp = 60,
    [int]$Duration = 300,
    [string]$HostUrl = "localhost",
    [int]$Port = 8080
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$JmxPath = Join-Path $ScriptDir "eams-load-test-500-1000-users.jmx"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$ResultsDir = Join-Path $ScriptDir "results_$Timestamp"
$JtlFile = Join-Path $ResultsDir "results.jtl"
$HtmlReportDir = Join-Path $ResultsDir "html-dashboard"

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "  EAMS High-Concurrency Load Test Execution Runner" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "Target Host     : http://${HostUrl}:${Port}"
Write-Host "Virtual Users   : $Threads concurrent threads"
Write-Host "Ramp-up Time    : $RampUp seconds"
Write-Host "Duration        : $Duration seconds"
Write-Host "Test Plan       : $JmxPath"
Write-Host "Output Dir      : $ResultsDir"
Write-Host "==================================================================" -ForegroundColor Cyan

# Check if JMeter command is on PATH
$JMeterCmd = Get-Command jmeter -ErrorAction SilentlyContinue

if (-not $JMeterCmd) {
    Write-Host "[!] JMeter executable not found in system PATH." -ForegroundColor Yellow
    Write-Host "[i] If JMeter is installed, specify the path or add it to PATH environment variable."
    Write-Host "[i] Example: $env:PATH += ';C:\apache-jmeter\bin'"
    Write-Host "[i] You can also open '$JmxPath' directly in JMeter GUI."
    Exit 1
}

# Create Results Directory
New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null
New-Item -ItemType Directory -Force -Path $HtmlReportDir | Out-Null

Write-Host "`n[+] Executing JMeter non-GUI load test..." -ForegroundColor Green

$JMeterArgs = @(
    "-n",
    "-t", $JmxPath,
    "-l", $JtlFile,
    "-e",
    "-o", $HtmlReportDir,
    "-Jhost=$HostUrl",
    "-Jport=$Port",
    "-Jthreads=$Threads",
    "-Jrampup=$RampUp",
    "-Jduration=$Duration"
)

& jmeter @JMeterArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n==================================================================" -ForegroundColor Green
    Write-Host "  LOAD TEST COMPLETED SUCCESSFULLY!" -ForegroundColor Green
    Write-Host "==================================================================" -ForegroundColor Green
    Write-Host "Raw Results (JTL)  : $JtlFile"
    Write-Host "HTML Dashboard     : $HtmlReportDir\index.html"
    Write-Host "`nOpening HTML Performance Report in your default browser..."
    Start-Process (Join-Path $HtmlReportDir "index.html")
} else {
    Write-Host "`n[!] JMeter execution exited with code $LASTEXITCODE" -ForegroundColor Red
}
