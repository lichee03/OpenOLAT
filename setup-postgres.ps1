# PostgreSQL Database Setup for OpenOLAT
# Run this script after entering your PostgreSQL postgres user password

Write-Host "Setting up OpenOLAT database..." -ForegroundColor Green
Write-Host "Please enter the password you set for 'postgres' user during PostgreSQL installation" -ForegroundColor Yellow

# Add PostgreSQL to PATH
$pgVersion = Get-ChildItem "C:\Program Files\PostgreSQL" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Name
if ($pgVersion) {
    $env:Path = "C:\Program Files\PostgreSQL\$pgVersion\bin;" + $env:Path
    Write-Host "PostgreSQL $pgVersion found" -ForegroundColor Cyan
}

# Create user and database
Write-Host "`nCreating 'openolat' user and database..." -ForegroundColor Cyan
psql -U postgres -h localhost -c "CREATE USER openolat WITH PASSWORD 'openolat';"
psql -U postgres -h localhost -c "CREATE DATABASE openolat OWNER openolat;"
psql -U postgres -h localhost -c "GRANT ALL PRIVILEGES ON DATABASE openolat TO openolat;"

# Connect as openolat user and initialize schema
Write-Host "`nInitializing database schema..." -ForegroundColor Cyan
$env:PGPASSWORD = "openolat"
psql -U openolat -h localhost -d openolat -f "src\main\resources\database\postgresql\setupDatabase.sql"

Write-Host "`nDatabase setup complete!" -ForegroundColor Green
Write-Host "Database: openolat" -ForegroundColor Cyan
Write-Host "User: openolat" -ForegroundColor Cyan
Write-Host "Password: openolat" -ForegroundColor Cyan
Write-Host "Port: 5432" -ForegroundColor Cyan
