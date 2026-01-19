# Step 2: Initialize OpenOLAT database schema
# Run this AFTER step 1 completes successfully

# Add PostgreSQL to PATH
$pgVersion = Get-ChildItem "C:\Program Files\PostgreSQL" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Name
if ($pgVersion) {
    $env:Path = "C:\Program Files\PostgreSQL\$pgVersion\bin;" + $env:Path
}

Write-Host "Initializing OpenOLAT database schema..." -ForegroundColor Cyan
Write-Host "Password for 'openolat' user is: openolat`n" -ForegroundColor Yellow

# Set password for openolat user
$env:PGPASSWORD = "openolat"

# Initialize schema
psql -U openolat -h localhost -d openolat -f "src\main\resources\database\postgresql\setupDatabase.sql"

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nDatabase initialization complete!" -ForegroundColor Green
    Write-Host "`nDatabase configuration:" -ForegroundColor Cyan
    Write-Host "  Host: localhost" -ForegroundColor White
    Write-Host "  Port: 5432" -ForegroundColor White
    Write-Host "  Database: openolat" -ForegroundColor White
    Write-Host "  Username: openolat" -ForegroundColor White
    Write-Host "  Password: openolat" -ForegroundColor White
    Write-Host "`nYou can now deploy OpenOLAT with: .\deploy-tomcat.ps1" -ForegroundColor Yellow
} else {
    Write-Host "`nDatabase initialization failed!" -ForegroundColor Red
    Write-Host "Please check the error messages above." -ForegroundColor Yellow
}
