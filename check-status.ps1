# OpenOLAT Troubleshooting Script

Write-Host "=== OpenOLAT Status Check ===" -ForegroundColor Green

# Check if Tomcat is running
Write-Host "`n1. Checking Tomcat process..." -ForegroundColor Cyan
$tomcatProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($tomcatProcess) {
    Write-Host "   ✓ Tomcat is running (PID: $($tomcatProcess.Id -join ', '))" -ForegroundColor Green
} else {
    Write-Host "   ✗ Tomcat is NOT running" -ForegroundColor Red
}

# Check if port 8080 is listening
Write-Host "`n2. Checking if port 8080 is listening..." -ForegroundColor Cyan
$port = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($port) {
    Write-Host "   ✓ Port 8080 is listening" -ForegroundColor Green
} else {
    Write-Host "   ✗ Port 8080 is NOT listening" -ForegroundColor Red
}

# Check HTTP response
Write-Host "`n3. Checking OpenOLAT HTTP response..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/olat/" -MaximumRedirection 0 -ErrorAction Stop -UseBasicParsing
    Write-Host "   ✓ HTTP Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "   Response: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Check database connection
Write-Host "`n4. Checking PostgreSQL database..." -ForegroundColor Cyan
$pgVersion = Get-ChildItem "C:\Program Files\PostgreSQL" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Name
if ($pgVersion) {
    $env:Path = "C:\Program Files\PostgreSQL\$pgVersion\bin;" + $env:Path
    $env:PGPASSWORD = "openolat"
    $result = psql -U openolat -h localhost -d openolat -c "SELECT COUNT(*) FROM o_bs_identity;" -t -A 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ Database connected. Users in database: $result" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Database connection error" -ForegroundColor Red
    }
}

# Check logs
Write-Host "`n5. Checking latest Tomcat logs..." -ForegroundColor Cyan
$latestLog = Get-ChildItem "C:\apache-tomcat-10.1.33\logs" -Filter "catalina.*.log" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($latestLog) {
    Write-Host "   Latest log: $($latestLog.Name)" -ForegroundColor White
    Write-Host "`n   Last 10 lines:" -ForegroundColor Yellow
    Get-Content $latestLog.FullName -Tail 10 | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
}

Write-Host "`n=== End of Status Check ===" -ForegroundColor Green
Write-Host "`nIf no users exist in database, OpenOLAT might still be initializing." -ForegroundColor Yellow
Write-Host "Wait 60 seconds and run this script again, or check the Tomcat console window for errors." -ForegroundColor Cyan
