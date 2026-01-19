# Step 1: Create OpenOLAT database and user
# This will prompt for postgres password

# Add PostgreSQL to PATH
$pgVersion = Get-ChildItem "C:\Program Files\PostgreSQL" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Name
if ($pgVersion) {
    $env:Path = "C:\Program Files\PostgreSQL\$pgVersion\bin;" + $env:Path
    Write-Host "Using PostgreSQL $pgVersion" -ForegroundColor Green
}

Write-Host "`nCreating OpenOLAT database..." -ForegroundColor Cyan
Write-Host "You will be prompted for the 'postgres' user password (the one you set during PostgreSQL installation)`n" -ForegroundColor Yellow

# Create user
psql -U postgres -h localhost -c "CREATE USER openolat WITH PASSWORD 'openolat';"

# Create database
psql -U postgres -h localhost -c "CREATE DATABASE openolat OWNER openolat;"

# Grant privileges
psql -U postgres -h localhost -c "GRANT ALL PRIVILEGES ON DATABASE openolat TO openolat;"

Write-Host "`nDatabase and user created successfully!" -ForegroundColor Green
