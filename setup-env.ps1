# OpenOLAT Development Environment Setup Script
# Run this script before working on OpenOLAT in a new terminal session

Write-Host "Setting up OpenOLAT development environment..." -ForegroundColor Green

# Set JAVA_HOME to JDK 17
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.13+11"
Write-Host "JAVA_HOME set to: $env:JAVA_HOME" -ForegroundColor Cyan

# Add JDK 17 and Maven to PATH
$env:Path = "C:\Program Files\Java\jdk-17.0.13+11\bin;C:\Program Files\Apache\Maven\bin;" + $env:Path
Write-Host "Updated PATH with Java 17 and Maven" -ForegroundColor Cyan

# Verify setup
Write-Host "`nVerifying Java version:" -ForegroundColor Yellow
java -version

Write-Host "`nVerifying Maven version:" -ForegroundColor Yellow
mvn -version

Write-Host "`nEnvironment setup complete!" -ForegroundColor Green
Write-Host "You can now run Maven commands like: mvn clean install -DskipTests=true -Ptomcat" -ForegroundColor Cyan
